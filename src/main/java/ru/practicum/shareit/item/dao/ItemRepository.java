package ru.practicum.shareit.item.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerIdOrderById(Long userId, Pageable page);

    List<Item> findByOwner(User owner);

    List<Item> findByRequestId(Long requestId);

    List<Item> findByRequestIdIn(List<Long> requestIds);

    @Query(" select i from Item i where i.available = true and " +
            "(upper(i.name) like upper(concat('%', ?1, '%'))" +
            "or upper(i.description) like upper(concat('%', ?1, '%')))")
    List<Item> getItemsText(String text, Pageable page);
}
