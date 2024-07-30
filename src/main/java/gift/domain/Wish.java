package gift.domain;

import static jakarta.persistence.FetchType.LAZY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Entity
public class Wish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @Column(nullable = false)
    private int count;

    protected Wish() {
    }

    public Wish(User user, Product product, int count) {
        this.user = user;
        this.product = product;
        this.count = count;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Product getProduct() {
        return product;
    }

    public int getCount() {
        return count;
    }

    public static class WishList {
        List<Wish> wishList;

        public WishList(List<Wish> wishList) {
            this.wishList = wishList;
        }

        public Optional<Wish> checkWishList(Product orderProduct) {
            return wishList.stream()
                .filter(wish -> wish.getProduct().getId().equals(orderProduct.getId()))
                .findFirst();
            }
        }


}