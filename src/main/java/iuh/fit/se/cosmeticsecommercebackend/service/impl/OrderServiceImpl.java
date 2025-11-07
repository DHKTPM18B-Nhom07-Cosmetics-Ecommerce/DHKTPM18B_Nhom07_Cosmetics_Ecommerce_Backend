package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.model.Customer;
import iuh.fit.se.cosmeticsecommercebackend.model.Employee;
import iuh.fit.se.cosmeticsecommercebackend.model.Order;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.OrderStatus;
import iuh.fit.se.cosmeticsecommercebackend.repository.OrderRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.OrderService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository repo;
    public OrderServiceImpl(OrderRepository repo){
        this.repo = repo;
    }
    @Override
    public Order createOrder(Order order){
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        return repo.save(order);
    }
    @Override
    public List<Order> findAll(){
        return repo.findAll();
    }
    @Override
    public Order findById(long id){
        return repo.findById(id).orElseThrow(()-> new NoSuchElementException("Khong tim thay don hang voi ID: "+id));
    }
    @Override
    public Order updateOrder(Long id, Order orderDetails){
        Order existingOrder = findById(id);
       // don hang o PENDING -> moi cho phep cap nhat:
        if(existingOrder.getStatus() != OrderStatus.PENDING){
            throw new IllegalStateException("Không thể chỉnh sửa chi tiết đơn khàng trừ trạng thái khi đơn hàng đã được xác nhận. Trang thái hiện tại: "+existingOrder.getStatus());
        }
        existingOrder.setTotal(orderDetails.getTotal());
        if(orderDetails.getCustomer()!=null){
            existingOrder.setCustomer(orderDetails.getCustomer());
        }
        return repo.save(existingOrder);
    }
    //PHUONG THUC TIM KIEM
    @Override
    public List<Order> findByCustomer(Customer customer){
        return repo.findByCustomer(customer);
    }
    @Override
    public List<Order> findByEmployee(Employee employee){
        return repo.findByEmployee(employee);
    }
    @Override
    public List<Order> findByStatus(OrderStatus status){
        return repo.findByStatus(status);
    }
    @Override
    public List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate){
        return repo.findByOrderDateBetween(startDate, endDate);
    }
    @Override
    public List<Order> findByStatusAndCustomer(OrderStatus status, Customer customer){
        return repo.findByStatusAndCustomer(status, customer);
    }
    @Override
    public List<Order> findByTotalBetween(BigDecimal minTotal, BigDecimal maxTotal){
        return repo.findByTotalBetween(minTotal, maxTotal);
    }
    //NGHIEP VU XU LY TRANG THAI
    @Override
    public Order cancelOrder(long id, String cancelReason, Employee employee){
        Order order = findById(id);
        //chi huy neu chua giao hoac chua duoc hoan tien
        if(order.getStatus() == OrderStatus.SHIPPING||
                order.getStatus()== OrderStatus.DELIVERED||
                order.getStatus() == OrderStatus.REFUNDED){
            throw new IllegalStateException("Không thể hủy đơn hàng đang giao, đã giao thành công hoặc đã hoàn tiền");

        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(cancelReason);
        order.setCanceledAt(LocalDateTime.now());
        if(employee!=null){
            order.setEmployee(employee);
        }
        return repo.save(order);
    }
    @Override
    public Order requestReturn(Long id, String returnReason, Employee employee){
        Order order =findById(id);
        if(employee==null){
            throw new IllegalStateException("Yêu cầu hoàn trả phải được 1 nhân viên ghi nhận lại.");

        }
        //chi hoan tra nhung don hang giao thanh cong
        if(order.getStatus()!=OrderStatus.DELIVERED){
            throw new IllegalStateException("Chỉ có thể yêu cầu hoàn trả đối với đơn hàng giao thành công.");
        }
        order.setStatus(OrderStatus.RETURNED);
        order.setEmployee(employee);
        return repo.save(order);
    }
    @Override
    public Order processRefund(Long id, Employee employee){
        Order order = findById(id);
        if(employee==null){
            throw new IllegalStateException("Xử lý hoàn tiền phải được thực hiện bơởi một nhan viên.");
        }
        //hoan tien neu trang thai là RETURNED
        if(order.getStatus()!=OrderStatus.RETURNED){
            throw new IllegalStateException("Chỉ hoàn tiền với đơn hàng đã được hoàn trả.");
        }
        order.setStatus(OrderStatus.REFUNDED);
        order.setEmployee(employee);
        return repo.save(order);
    }
    @Override
    public Order updateStatus(Long id, OrderStatus newStatus, String cancelReason, Employee employee){
        if(newStatus == OrderStatus.CANCELLED){
            return cancelOrder(id, cancelReason, employee);
        }
        //yeu cau nhan vien cho cac thay doi trang thai quan ly
        if(employee==null && newStatus != OrderStatus.PENDING){
            throw new IllegalArgumentException("Thay đổi trạng thái sang"+newStatus+"phải được thực hiện bởi 1 nhân viên");
        }
        Order order = findById(id);
        OrderStatus currentStatus = order.getStatus();
        //luong xu ly
        switch (newStatus) {
            case CONFIRMED:
                if (currentStatus != OrderStatus.PENDING) {
                    throw new IllegalStateException("Không thể chuyển từ " + currentStatus + " sang CONFIRMED. Chỉ cho phép từ PENDING.");
                }
                break;
            case PROCESSING:
                if (currentStatus != OrderStatus.CONFIRMED) {
                    throw new IllegalStateException("Không thể chuyển từ " + currentStatus + " sang PROCESSING. Chỉ cho phép từ CONFIRMED.");
                }
                break;
            case SHIPPING:
                if (currentStatus != OrderStatus.PROCESSING) {
                    throw new IllegalStateException("Không thể chuyển từ " + currentStatus + " sang SHIPPING. Chỉ cho phép từ PROCESSING.");
                }
                break;
            case DELIVERED:
                if (currentStatus != OrderStatus.SHIPPING) {
                    throw new IllegalStateException("Không thể chuyển từ " + currentStatus + " sang DELIVERED. Chỉ cho phép từ SHIPPING.");
                }
                break;
            default:
                break;
        }

        order.setStatus(newStatus);

        if (employee != null) {
            order.setEmployee(employee);
        }
        return repo.save(order);
    }



}
