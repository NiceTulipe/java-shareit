package ru.practicum.shareit.booking.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 order by b.start desc ")
    List<Booking> findAllByOwnerItems(@Param("owner") long ownerId, Pageable page);

    @Query("select b from Booking b " +
            "join User as u on b.booker = u.id " +
            "where b.item = ?1 and b.status = ?2 and u.id = ?3 and b.end < ?4")
    List<Booking> findBookingsByItem(
            Item item,
            BookingStatus status,
            Long idUser,
            LocalDateTime dateTime);

    @Query("SELECT b from Booking b where b.item.owner.id = ?1 AND b.status = ?2 order by b.start desc")
    List<Booking> findByOwnerAndState(
            long userId,
            BookingStatus status,
            Pageable page);

    @Query("select b " +
            "from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and ?2 > b.end order by b.start desc")
    List<Booking> findAllPastBookingsOwner(
            Long ownerId,
            LocalDateTime time,
            Pageable page);

    @Query("select b " +
            "from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and ?2 < b.start order by b.start desc")
    List<Booking> findAllFutureBookingsOwner(
            Long ownerId,
            LocalDateTime time,
            Pageable page);

    @Query("select b " +
            "from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and ?2 between b.start and b.end " +
            "order by b.start ")
    List<Booking> findAllCurrentBookingsOwner(
            Long ownerId,
            LocalDateTime time,
            Pageable page);

    List<Booking> findAllByBooker_IdOrderByStartDesc(long id, Pageable page);

    List<Booking> findAllByBooker_IdAndStatusIsOrderByStartDesc(
            Long id,
            BookingStatus status,
            Pageable page);

    List<Booking> findAllByBooker_IdAndEndIsAfterAndStartIsBeforeOrderByStart(
            Long id,
            LocalDateTime end,
            LocalDateTime start,
            Pageable page);

    List<Booking> findAllByBooker_IdAndEndIsBeforeOrderByStartDesc(
            Long id,
            LocalDateTime time,
            Pageable page);

    List<Booking> findAllByBooker_IdAndStartIsAfterOrderByStartDesc(
            Long id,
            LocalDateTime time,
            Pageable page);

    List<Booking> findAllByBooker_IdAndStartIsAfterAndStatusIsOrderByStartDesc(
            Long bookerId,
            LocalDateTime start,
            BookingStatus status,
            Pageable page);

    List<Booking> findByItemInAndStatus(
            List<Item> items,
            BookingStatus status,
            Sort created);
}