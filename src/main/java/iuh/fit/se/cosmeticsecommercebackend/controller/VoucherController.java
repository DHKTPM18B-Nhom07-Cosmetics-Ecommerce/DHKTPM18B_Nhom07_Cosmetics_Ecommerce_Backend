package iuh.fit.se.cosmeticsecommercebackend.controller;

import iuh.fit.se.cosmeticsecommercebackend.model.Voucher;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.VoucherScope;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.VoucherStatus;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.VoucherType;
import iuh.fit.se.cosmeticsecommercebackend.service.VoucherService;

import org.apache.poi.ss.usermodel.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {

    private final VoucherService service;

    public VoucherController(VoucherService service) {
        this.service = service;
    }

    /* ============================================================
                              CRUD
       ============================================================ */

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            Voucher v = extractVoucher(body);

            Voucher saved = service.create(
                    v,
                    parseIds(body.get("categoryIds")),
                    parseIds(body.get("brandIds")),
                    parseIds(body.get("productIds"))
            );

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Voucher v = extractVoucher(body);

            Voucher saved = service.update(
                    id,
                    v,
                    parseIds(body.get("categoryIds")),
                    parseIds(body.get("brandIds")),
                    parseIds(body.get("productIds"))
            );

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        try {
            VoucherStatus newStatus = VoucherStatus.valueOf(body.get("status"));
            return ResponseEntity.ok(service.updateStatus(id, newStatus));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /* ============================================================
                        BULK UPLOAD – OPTION C
       ============================================================ */

    @PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> bulkUpload(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty())
                return ResponseEntity.badRequest().body(Map.of("success", 0, "errors",
                        List.of(Map.of("row", 0, "error", "File rỗng"))));

            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            List<Map<String, Object>> rows = new ArrayList<>();
            List<Map<String, Object>> errors = new ArrayList<>();
            Set<String> fileCodes = new HashSet<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, Object> rowMap = mapExcelRow(row);
                String err = validateRow(rowMap, fileCodes);

                if (err != null) {
                    errors.add(Map.of("row", i + 1, "error", err));
                    continue;
                }

                fileCodes.add(rowMap.get("code").toString());
                rows.add(rowMap);
            }

            workbook.close();

            if (!errors.isEmpty())
                return ResponseEntity.badRequest().body(Map.of("success", 0, "errors", errors));

            int imported = service.importBulk(rows);

            return ResponseEntity.ok(Map.of("success", imported, "errors", List.of()));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", 0,
                    "errors", List.of(Map.of("row", 0, "error", "Lỗi xử lý file: " + e.getMessage()))
            ));
        }
    }

    /* ============================================================
                          VALIDATION
       ============================================================ */

    private String validateRow(Map<String, Object> m, Set<String> fileCodes) {
        try {
            /* -------- CODE -------- */
            if (isEmpty(m.get("code"))) return "Thiếu mã voucher";

            String code = m.get("code").toString().trim().toUpperCase();

            if (!code.matches("^[A-Z0-9_]+$"))
                return "Mã voucher chỉ được chứa A–Z, 0–9, _";

            if (fileCodes.contains(code))
                return "Mã '" + code + "' bị trùng trong file";

            boolean exists = service.getAll().stream()
                    .anyMatch(v -> v.getCode().equalsIgnoreCase(code));

            if (exists) return "Mã '" + code + "' đã tồn tại trong DB";

            /* -------- TYPE -------- */
            VoucherType type;
            try {
                type = VoucherType.valueOf(m.get("type").toString().trim().toUpperCase());
            } catch (Exception ex) {
                return "Type không hợp lệ";
            }

            /* -------- VALUE -------- */
            if (isEmpty(m.get("value"))) return "Thiếu value";

            BigDecimal value = safeDecimal(m.get("value"));
            if (value.compareTo(BigDecimal.ZERO) < 0)
                return "Value không được âm";

            /* PERCENT RULE */
            if (type == VoucherType.PERCENT) {
                if (isEmpty(m.get("maxDiscount")))
                    return "Giảm % bắt buộc phải có maxDiscount";

                BigDecimal max = safeDecimal(m.get("maxDiscount"));

                if (value.compareTo(BigDecimal.ONE) < 0 || value.compareTo(BigDecimal.valueOf(100)) > 0)
                    return "Value % phải từ 1 đến 100";

                if (max.compareTo(BigDecimal.ZERO) <= 0)
                    return "maxDiscount phải > 0";
            }

            /* SHIPPING FREE RULE */
            if (type == VoucherType.SHIPPING_FREE && value.compareTo(BigDecimal.ZERO) != 0)
                return "SHIPPING_FREE yêu cầu value = 0";

            /* -------- LIMIT -------- */
            Integer maxUses = safeInt(m.get("maxUses"));
            if (maxUses != null && maxUses <= 0)
                return "maxUses phải > 0";

            Integer perUser = safeInt(m.get("perUserLimit"));
            if (perUser != null && perUser <= 0)
                return "perUserLimit phải > 0";

            /* -------- STACKABLE -------- */
            if (isEmpty(m.get("stackable")))
                return "Thiếu stackable";

            String s = m.get("stackable").toString().trim().toLowerCase();
            if (!(s.equals("true") || s.equals("false")))
                return "stackable phải là true/false";

            /* -------- SCOPE -------- */
            VoucherScope scope;
            try {
                scope = VoucherScope.valueOf(m.get("scope").toString().trim().toUpperCase());
            } catch (Exception ex) {
                return "Scope không hợp lệ";
            }

            List<Long> cat = parseIds(m.get("categoryIds"));
            List<Long> brand = parseIds(m.get("brandIds"));
            List<Long> prod = parseIds(m.get("productIds"));

            if (scope == VoucherScope.CATEGORY && cat.isEmpty())
                return "Scope=CATEGORY nhưng thiếu categoryIds";

            if (scope == VoucherScope.BRAND && brand.isEmpty())
                return "Scope=BRAND nhưng thiếu brandIds";

            if (scope == VoucherScope.PRODUCT && prod.isEmpty())
                return "Scope=PRODUCT nhưng thiếu productIds";

            /* -------- DATE -------- */
            LocalDateTime start = parseDate(m.get("startAt"));
            LocalDateTime end = parseDate(m.get("endAt"));

            if (start == null || end == null)
                return "Ngày không hợp lệ";

            if (!end.isAfter(start))
                return "endAt phải > startAt";

            return null;

        } catch (Exception e) {
            return "Lỗi parse: " + e.getMessage();
        }
    }

    /* ============================================================
                        MAP → ENTITY
       ============================================================ */

    private Voucher extractVoucher(Map<String, Object> body) {
        Voucher v = new Voucher();

        v.setCode(body.get("code").toString()); // CODE KHÔNG UPDATE
        v.setType(VoucherType.valueOf(body.get("type").toString().trim().toUpperCase()));
        v.setScope(VoucherScope.valueOf(body.get("scope").toString().trim().toUpperCase()));

        v.setValue(safeDecimal(body.get("value")));

        // maxDiscount = null nếu FE không truyền
        v.setMaxDiscount(isEmpty(body.get("maxDiscount"))
                ? null
                : safeDecimal(body.get("maxDiscount")));

        v.setMinOrderAmount(safeDecimal(body.get("minOrderAmount")));

        v.setStartAt(parseDate(body.get("startAt")));
        v.setEndAt(parseDate(body.get("endAt")));

        v.setMaxUses(safeInt(body.get("maxUses")));
        v.setPerUserLimit(safeInt(body.get("perUserLimit")));
        v.setStackable(safeBoolean(body.get("stackable")));

        v.setStatus(VoucherStatus.UPCOMING);

        return v;
    }

    /* ============================================================
                           HELPERS
       ============================================================ */

    private Map<String, Object> mapExcelRow(Row row) {
        Map<String, Object> m = new HashMap<>();
        String[] keys = {
                "code", "type", "value", "maxDiscount", "minOrderAmount",
                "maxUses", "perUserLimit", "stackable", "scope",
                "startAt", "endAt", "categoryIds", "brandIds", "productIds"
        };

        for (int i = 0; i < keys.length; i++)
            m.put(keys[i], getCell(row, i));

        return m;
    }

    private String getCell(Row row, int index) {
        Cell c = row.getCell(index);
        return (c == null) ? null : c.toString().trim();
    }

    private boolean isEmpty(Object o) {
        return o == null || o.toString().trim().isEmpty();
    }

    private BigDecimal safeDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        return new BigDecimal(o.toString().trim());
    }

    private Integer safeInt(Object o) {
        if (o == null) return null;

        String s = o.toString().trim();
        if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);

        try {
            return Integer.parseInt(s);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Boolean safeBoolean(Object o) {
        return o != null && o.toString().trim().equalsIgnoreCase("true");
    }

    private LocalDateTime parseDate(Object o) {
        if (o == null) return null;

        if (o instanceof Double d) {
            Date date = DateUtil.getJavaDate(d);
            return LocalDateTime.ofInstant(date.toInstant(),
                    java.time.ZoneId.systemDefault());
        }

        String s = o.toString().trim();
        if (s.length() == 10)
            s += "T00:00:00";
        else if (s.length() == 16)
            s = s.replace(" ", "T") + ":00";
        else if (s.length() == 19)
            s = s.replace(" ", "T");

        return LocalDateTime.parse(s);
    }

    private List<Long> parseIds(Object obj) {

        if (obj == null) return List.of();

        // CASE 1: FE gửi list dạng JSON array
        if (obj instanceof List<?> list) {
            List<Long> ids = new ArrayList<>();
            for (Object item : list) {
                try {
                    ids.add(Long.parseLong(item.toString()));
                } catch (Exception ignore) {}
            }
            return ids;
        }

        // CASE 2: FE gửi string "1,2,3"
        String raw = obj.toString().trim()
                .replace("[", "")
                .replace("]", "");

        if (raw.isEmpty()) return List.of();

        List<Long> ids = new ArrayList<>();
        for (String part : raw.split(",")) {
            try {
                ids.add(Long.parseLong(part.trim()));
            } catch (Exception ignore) {}
        }

        return ids;
    }

}
