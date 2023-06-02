package ru.practicum.shareit.booking.dao;

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
    List<Booking> findAllByOwnerItems(@Param("owner") long ownerId);

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
            BookingStatus status);

    @Query("select b " +
            "from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and ?2 > b.end order by b.start desc")
    List<Booking> findAllPastBookingsOwner(
            Long ownerId,
            LocalDateTime time);


    @Query("select b " +
            "from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and ?2 < b.start order by b.start desc")
    List<Booking> findAllFutureBookingsOwner(
            Long ownerId,
            LocalDateTime time);

    @Query("select b " +
            "from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and ?2 between b.start and b.end")
    List<Booking> findAllCurrentBookingsOwner(
            Long ownerId,
            LocalDateTime time);

    List<Booking> findAllByBooker_IdOrderByStartDesc(long id);

    List<Booking> findAllByBooker_IdAndStatusIsOrderByStartDesc(
            Long id,
            BookingStatus status);

    List<Booking> findAllByBooker_IdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(
            Long id,
            LocalDateTime end,
            LocalDateTime start);

    List<Booking> findAllByBooker_IdAndEndIsBeforeOrderByStartDesc(
            Long id,
            LocalDateTime time);

    List<Booking> findAllByBooker_IdAndStartIsAfterOrderByStartDesc(
            Long id,
            LocalDateTime time);

    List<Booking> findAllByBooker_IdAndStartIsAfterAndStatusIsOrderByStartDesc(
            Long bookerId,
            LocalDateTime start,
            BookingStatus status);

    List<Booking> findByItemInAndStatus(
            List<Item> items,
            BookingStatus status,
            Sort created);
}
