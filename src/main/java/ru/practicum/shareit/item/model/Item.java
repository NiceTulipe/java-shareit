package ru.practicum.shareit.item.model;

import lombok.*;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import javax.validation.constraints.Positive;

/**
 * TODO Sprint add-controllers.
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
@Entity
@Table(name = "items")
@Builder
public class Item {
    @Positive
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "available", nullable = false)
    private Boolean available;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "request")
    private Long requestId;
}
