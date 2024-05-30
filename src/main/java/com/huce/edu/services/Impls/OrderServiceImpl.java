package com.huce.edu.services.Impls;

import com.huce.edu.entities.OrderEntity;
import com.huce.edu.entities.ProductEntity;
import com.huce.edu.entities.UserEntity;
import com.huce.edu.models.dto.OrderDto;
import com.huce.edu.models.response.OrderHistoryResponse;
import com.huce.edu.repositories.OrderRepo;
import com.huce.edu.repositories.ProductRepo;
import com.huce.edu.repositories.UserAccountRepo;
import com.huce.edu.services.OrderService;
import com.huce.edu.shareds.Constants;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final ProductRepo productRepo;
    private final OrderRepo orderRepo;
    private final UserAccountRepo userAccountRepo;

    @Override
    @Transactional
    public OrderEntity add(OrderDto orderDto, UserEntity user) {
        ProductEntity product = productRepo.findFirstByPid(orderDto.getPid());

        OrderEntity newOrder = OrderEntity.create(
                0,
                user.getUid(),
                Constants.getCurrentDay(),
                orderDto.getAddress(),
                orderDto.getPid(),
                product.getPrice(),
                orderDto.getQuantity(),
                orderDto.getPhone()
        );
        orderRepo.save(newOrder);

        user.setCoin(user.getCoin()-(orderDto.getQuantity()*product.getPrice()));
        userAccountRepo.save(user);

        product.setRemain(product.getRemain()-orderDto.getQuantity());
        productRepo.save(product);

        return newOrder;
    }
    @Override
    public ArrayList<Map<String, Object>> getAllOrderDetail(){
        ArrayList<Map<String, Object>> orderDetails = new ArrayList<>();
        List<OrderEntity> order = orderRepo.findAll();
        for(OrderEntity o : order){
            Map<String, Object> detail = new HashMap<>();
            detail.put("oid", o.getOid());
            detail.put("date", o.getDate());
            detail.put("quantity", o.getQuantity());
            detail.put("address", o.getAddress());
            detail.put("pname", productRepo.findFirstByPid(o.getPid()).getName());
            orderDetails.add(detail);
        }
        return orderDetails;
    }


    @Override
    public List<OrderHistoryResponse> getOrderHistory(Integer uid) {
        List<OrderHistoryResponse> orderHistoryResponses = new ArrayList<>();
        List<OrderEntity> orderEntities = orderRepo.findByUid(uid);
        for(OrderEntity o : orderEntities){
            ProductEntity p = productRepo.findFirstByPid(o.getPid());
            OrderHistoryResponse temp = OrderHistoryResponse.create(
                    o.getDate(),
                    o.getAddress(),
                    o.getPrice(),
                    o.getQuantity(),
                    o.getPhone(),
                    p.getName(),
                    p.getImage()
            );
            orderHistoryResponses.add(temp);
        }
        orderHistoryResponses.sort(Collections.reverseOrder(Comparator.comparing(OrderHistoryResponse::getDate)));
        return orderHistoryResponses;
    }
}
