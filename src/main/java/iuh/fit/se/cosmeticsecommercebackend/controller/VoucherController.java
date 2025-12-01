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

    /* ============================
            BASIC CRUD
       ============================ */

    @GetMapping({"", "/"})
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping({"", "/"})
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            Voucher v = extractVoucher(body);

            return ResponseEntity.ok(service.create(
                    v,
                    parseIds(body.get("categoryIds")),
                    parseIds(body.get("brandIds")),
                    parseIds(body.get("productIds"))
            ));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody Map<String, Object> body) {
        try {
            Voucher v = extractVoucher(body);

            return ResponseEntity.ok(service.update(
                    id,
                    v,
                    parseIds(body.get("categoryIds")),
                    parseIds(body.get("brandIds")),
                    parseIds(body.get("productIds"))
            ));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        try {
            VoucherStatus s = VoucherStatus.valueOf(body.get("status"));
            return ResponseEntity.ok(service.updateStatus(id, s));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }


    /* ============================
            BULK UPLOAD EXCEL
       ============================ */

    @PostMapping(
            value = "/bulk-upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> bulkUpload(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File rỗng!");
            }

            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            List<Map<String, Object>> successList = new ArrayList<>();
            List<Map<String, Object>> errorList = new ArrayList<>();

            // để check trùng trong file
            Set<String> fileCodes = new HashSet<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    Map<String, Object> rowMap = mapExcelRow(row);

                    String error = validateRow(rowMap, fileCodes);
                    if (error != null) {
                        errorList.add(Map.of(
                                "row", i + 1,
                                "error", error
                        ));
                        continue;
                    }

                    // add vào set để tránh code trùng trong file
                    fileCodes.add(rowMap.get("code").toString());

                    Voucher v = extractVoucher(rowMap);

                    List<Long> categoryIds = parseIds(rowMap.get("categoryIds"));
                    List<Long> brandIds = parseIds(rowMap.get("brandIds"));
                    List<Long> productIds = parseIds(rowMap.get("productIds"));

                    Voucher created = service.create(v, categoryIds, brandIds, productIds);

                    successList.add(Map.of(
                            "row", i + 1,
                            "code", created.getCode()
                    ));

                } catch (Exception ex) {
                    errorList.add(Map.of(
                            "row", i + 1,
                            "error", "Lỗi không xác định: " + ex.getMessage()
                    ));
                }
            }

            workbook.close();

            return ResponseEntity.ok(Map.of(
                    "success", successList,
                    "errors", errorList
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi xử lý file Excel: " + e.getMessage());
        }
    }


    /* ============================
          VALIDATION
       ============================ */

    private String validateRow(Map<String, Object> m, Set<String> fileCodes) {
        try {
            // CODE
            if (isEmpty(m.get("code"))) return "Thiếu mã voucher";
            String code = m.get("code").toString().trim();

            // TRÙNG CODE TRONG FILE
            if (fileCodes.contains(code)) {
                return "Mã '" + code + "' bị trùng trong file Excel";
            }

            // TRÙNG CODE TRONG DB
            boolean exists = service.getAll().stream()
                    .anyMatch(v -> v.getCode().equalsIgnoreCase(code));

            if (exists) return "Mã '" + code + "' đã tồn tại trong hệ thống";

            // TYPE / SCOPE
            if (isEmpty(m.get("type"))) return "Thiếu type";
            if (isEmpty(m.get("scope"))) return "Thiếu scope";

            VoucherType type = VoucherType.valueOf(m.get("type").toString().trim().toUpperCase());
            VoucherScope scope = VoucherScope.valueOf(m.get("scope").toString().trim().toUpperCase());

            // VALUE
            if (isEmpty(m.get("value"))) return "Thiếu giá trị giảm";

            // maxDiscount required for %
            if (type == VoucherType.PERCENT && isEmpty(m.get("maxDiscount")))
                return "Giảm % phải có maxDiscount";

            // Scope IDs
            if (scope == VoucherScope.CATEGORY && parseIds(m.get("categoryIds")).isEmpty())
                return "Scope=CATEGORY nhưng thiếu categoryIds";

            if (scope == VoucherScope.BRAND && parseIds(m.get("brandIds")).isEmpty())
                return "Scope=BRAND nhưng thiếu brandIds";

            if (scope == VoucherScope.PRODUCT && parseIds(m.get("productIds")).isEmpty())
                return "Scope=PRODUCT nhưng thiếu productIds";

            // validate date
            LocalDateTime start = parseDate(m.get("startAt"));
            LocalDateTime end = parseDate(m.get("endAt"));

            if (start == null || end == null)
                return "Ngày không hợp lệ";

            if (!end.isAfter(start))
                return "endAt phải > startAt";

        } catch (Exception e) {
            return "Lỗi parse dữ liệu: " + e.getMessage();
        }

        return null;
    }


    /* ============================
             MAP → ENTITY
       ============================ */

    private Voucher extractVoucher(Map<String, Object> body) {
        Voucher v = new Voucher();

        v.setCode(body.get("code").toString());
        v.setType(VoucherType.valueOf(body.get("type").toString().trim().toUpperCase()));
        v.setScope(VoucherScope.valueOf(body.get("scope").toString().trim().toUpperCase()));

        v.setValue(safeDecimal(body.get("value")));
        v.setMaxDiscount(safeDecimal(body.get("maxDiscount")));
        v.setMinOrderAmount(safeDecimal(body.get("minOrderAmount")));

        v.setStartAt(parseDate(body.get("startAt")));
        v.setEndAt(parseDate(body.get("endAt")));

        v.setMaxUses(safeInt(body.get("maxUses")));
        v.setPerUserLimit(safeInt(body.get("perUserLimit")));

        v.setStackable(safeBoolean(body.get("stackable")));

        // NEW voucher luôn = UPCOMING
        v.setStatus(VoucherStatus.UPCOMING);

        return v;
    }


    /* ============================
               HELPERS
       ============================ */

    private Map<String, Object> mapExcelRow(Row row) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", getCell(row, 0));
        m.put("type", getCell(row, 1));
        m.put("value", getCell(row, 2));
        m.put("maxDiscount", getCell(row, 3));
        m.put("minOrderAmount", getCell(row, 4));
        m.put("maxUses", getCell(row, 5));
        m.put("perUserLimit", getCell(row, 6));
        m.put("stackable", getCell(row, 7));
        m.put("scope", getCell(row, 8));
        m.put("startAt", getCell(row, 9));
        m.put("endAt", getCell(row, 10));
        m.put("categoryIds", getCell(row, 11));
        m.put("brandIds", getCell(row, 12));
        m.put("productIds", getCell(row, 13));
        return m;
    }

    private BigDecimal safeDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        String s = o.toString().trim();
        if (s.isEmpty()) return BigDecimal.ZERO;
        return new BigDecimal(s);
    }

    private Integer safeInt(Object o) {
        if (o == null) return null;
        String s = o.toString().trim();
        if (s.isEmpty()) return null;
        try {
            return Integer.parseInt(s);
        } catch (Exception ignore) {
            try {
                return Double.valueOf(s).intValue();
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private Boolean safeBoolean(Object o) {
        if (o == null) return false;
        return o.toString().trim().equalsIgnoreCase("true");
    }

    private LocalDateTime parseDate(Object o) {
        if (o == null) return null;

        String s = o.toString().trim();
        if (s.isEmpty()) return null;

        // yyyy-MM-dd
        if (s.length() == 10 && !s.contains("T")) {
            s = s + "T00:00:00";
        }
        // yyyy-MM-dd HH:mm
        else if (s.length() == 16 && s.contains(" ")) {
            s = s.replace(" ", "T") + ":00";
        }
        // yyyy-MM-dd HH:mm:ss
        else if (s.contains(" ") && s.length() == 19) {
            s = s.replace(" ", "T");
        }

        return LocalDateTime.parse(s);
    }

    private List<Long> parseIds(Object obj) {
        if (obj == null) return List.of();

        if (obj instanceof List<?> list) {
            return list.stream()
                    .map(x -> {
                        String raw = x.toString().trim();
                        if (raw.endsWith(".0")) {
                            raw = raw.substring(0, raw.length() - 2);
                        }
                        return Long.valueOf(raw);
                    })
                    .toList();
        }

        String s = obj.toString().trim();
        if (s.isBlank()) return List.of();

        List<Long> ids = new ArrayList<>();
        for (String p : s.split(",")) {
            try {
                String raw = p.trim();
                if (raw.endsWith(".0")) {
                    raw = raw.substring(0, raw.length() - 2);
                }
                ids.add(Long.valueOf(raw));
            } catch (Exception ignore) {
            }
        }
        return ids;
    }

    private String getCell(Row row, int idx) {
        Cell c = row.getCell(idx);
        if (c == null) return null;
        return c.toString().trim();
    }

    private boolean isEmpty(Object o) {
        return o == null || o.toString().trim().isEmpty();
    }
}
