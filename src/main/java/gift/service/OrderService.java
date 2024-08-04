package gift.service;

import gift.auth.KakaoClient;
import gift.domain.Option;
import gift.domain.Order;
import gift.domain.User;
import gift.domain.Wish.WishList;
import gift.dto.common.PageInfo;
import gift.dto.requestdto.OrderRequestDTO;
import gift.dto.responsedto.OrderPageResponseDTO;
import gift.dto.responsedto.OrderResponseDTO;
import gift.repository.JpaOrderRepository;
import gift.repository.JpaWishRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class OrderService {
    private final JpaOrderRepository jpaOrderRepository;
    private final JpaWishRepository jpaWishRepository;
    private final KakaoClient kakaoClient;
    private static final double DISCOUNT_RATE = 0.05;

    public OrderService(JpaOrderRepository jpaOrderRepository, JpaWishRepository jpaWishRepository,
        KakaoClient kakaoClient) {
        this.jpaOrderRepository = jpaOrderRepository;
        this.jpaWishRepository = jpaWishRepository;
        this.kakaoClient = kakaoClient;
    }

    public OrderResponseDTO order(OrderRequestDTO orderRequestDTO, User user, Option option){
        option.subtract(orderRequestDTO.quantity());

        WishList wishList = new WishList(jpaWishRepository.findAllByUserId(user.getId()));
        wishList.checkWishList(option.getProduct())
            .ifPresent(jpaWishRepository::delete);

        int orderPrice = orderRequestDTO.quantity() * option.getProduct().getPrice();
        user.addPoint((int)(orderPrice * DISCOUNT_RATE));

        if (orderPrice < orderRequestDTO.point()){
            throw new IllegalArgumentException("사용하려는 포인트가 주문 금액보다 많습니다.");
        }

        orderPrice -= orderRequestDTO.point();
        user.usePoint(orderRequestDTO.point());

        Order order = orderRequestDTO.toEntity(user, option, orderPrice);
        jpaOrderRepository.save(order);

        kakaoClient.sendMessage(user.getAccessToken(), orderRequestDTO.message());
        return OrderResponseDTO.from(order);
    }

    public OrderPageResponseDTO getAllOrders(Long userId, int page, int size, String criteria) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(criteria));

        Page<Order> orderPage = jpaOrderRepository.findAllByUserId(userId, pageable);

        List<OrderResponseDTO> orderResponseDTOList = orderPage
            .stream()
            .map(OrderResponseDTO::from)
            .toList();

        PageInfo pageInfo = new PageInfo(page, orderPage.getTotalElements(), orderPage.getTotalPages());

        return new OrderPageResponseDTO(pageInfo, orderResponseDTOList);

    }
}
