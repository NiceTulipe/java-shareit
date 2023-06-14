package ru.practicum.shareit.booking;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
public class BookingRepositoryTest {

    @Autowired
    private final BookingRepository bookingRepository = null;
    @Autowired
    protected TestEntityManager entityManager;

    public static User makeUser(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    public static Item makeItem(Long id, String name, String description, User user, boolean available) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setDescription(description);
        item.setOwner(user);
        item.setAvailable(available);
        return item;
    }

    public static Booking makeBooking(
            Long id,
            LocalDateTime start,
            LocalDateTime end,
            Item item,
            User user,
            BookingStatus status
    ) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(status);
        return booking;
    }

    @Test
    public void shouldFindNoBookingsIfRepositoryIsEmpty() {
        Iterable<Booking> bookings = bookingRepository.findAll();

        assertThat(bookings).isEmpty();
    }

    @Test
    public void shouldStoreBooking() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        User owner = entityManager.persist(makeUser(null,
                "Ash",
                "ash@gmail.com"));
        User booker = entityManager.persist(makeUser(null,
                "Misty",
                "misty@gmail.com"));
        Item item = entityManager.persist(makeItem(null,
                "Poke Ball",
                "The Poke Ball is a sphere",
                owner,
                true));
        Booking booking = bookingRepository.save(makeBooking(null,
                start,
                end,
                item,
                booker,
                BookingStatus.WAITING));

        AssertionsForClassTypes.assertThat(booking)
                .hasFieldOrPropertyWithValue("start", start)
                .hasFieldOrPropertyWithValue("end", end)
                .hasFieldOrPropertyWithValue("status", BookingStatus.WAITING)
                .hasFieldOrProperty("item")
                .hasFieldOrProperty("booker");
        AssertionsForClassTypes.assertThat(booking.getItem())
                .isInstanceOf(Item.class)
                .hasFieldOrPropertyWithValue("name", "Poke Ball");
    }

    @Test
    public void shouldCurrentByOwnerId() {
        LocalDateTime now = LocalDateTime.now();
        User owner = entityManager.persist(makeUser(null,
                "Ash",
                "ash@gmail.com"));
        User booker = entityManager.persist(makeUser(null,
                "Misty",
                "misty@gmail.com"));
        Item item1 = entityManager.persist(makeItem(null,
                "Poke Ball",
                "The Poke Ball is a sphere",
                owner,
                true));
        Item item2 = entityManager.persist(makeItem(null,
                "Ultra Ball",
                "is a Poke Ball that has a 2x catch rate modifier",
                owner,
                true));
        entityManager.persist(makeBooking(null,
                now.minusDays(1),
                now.plusDays(1),
                item1,
                booker,
                BookingStatus.WAITING));
        entityManager.persist(makeBooking(null,
                now.plusDays(1),
                now.plusDays(2),
                item2,
                booker,
                BookingStatus.WAITING));

        Pageable pageable = PageRequest.of(0, 20);
        List<Booking> listBookings = bookingRepository.findAllCurrentBookingsOwner(owner.getId(),
                LocalDateTime.now(), pageable);

        assertThat(listBookings)
                .hasSize(1)
                .element(0)
                .hasFieldOrProperty("item");
        AssertionsForClassTypes.assertThat(listBookings.get(0).getItem())
                .isInstanceOf(Item.class)
                .hasFieldOrPropertyWithValue("name", "Poke Ball");
    }

    @Test
    public void shouldFindPastByOwner() {
        LocalDateTime now = LocalDateTime.now();
        User owner1 = entityManager.persist(makeUser(null,
                "Ash",
                "ash@gmail.com"));
        User owner2 = entityManager.persist(makeUser(null,
                "Misty",
                "misty@gmail.com"));
        Item item1 = entityManager.persist(makeItem(null,
                "Poke Ball",
                "The Poke Ball is a sphere",
                owner1,
                true));
        Item item2 = entityManager.persist(makeItem(null,
                "Ultra Ball",
                "is a Poke Ball that has a 2x catch rate modifier",
                owner2,
                true));
        entityManager.persist(makeBooking(null,
                now.minusDays(2),
                now.minusDays(1),
                item1,
                owner1,
                BookingStatus.WAITING));
        entityManager.persist(makeBooking(null,
                now.minusDays(3),
                now.minusDays(2),
                item2,
                owner2,
                BookingStatus.WAITING));

        Pageable pageable = PageRequest.of(0, 20);
        List<Booking> listBookings = bookingRepository.findAllPastBookingsOwner(owner1.getId(),
                LocalDateTime.now(), pageable);

        assertThat(listBookings)
                .hasSize(1)
                .element(0)
                .hasFieldOrProperty("item");
        AssertionsForClassTypes.assertThat(listBookings.get(0).getItem())
                .isInstanceOf(Item.class)
                .hasFieldOrPropertyWithValue("name", "Poke Ball");
    }

    @Test
    public void shouldFindFutureByOwnerId() {
        LocalDateTime now = LocalDateTime.now();
        User owner = entityManager.persist(makeUser(null,
                "Ash",
                "ash@gmail.com"));
        User booker = entityManager.persist(makeUser(null,
                "Misty",
                "misty@gmail.com"));
        Item item1 = entityManager.persist(makeItem(null,
                "Poke Ball",
                "The Poke Ball is a sphere",
                owner,
                true));
        Item item2 = entityManager.persist(makeItem(null,
                "Ultra Ball",
                "is a Poke Ball that has a 2x catch rate modifier",
                owner,
                true));
        entityManager.persist(makeBooking(null,
                now.minusDays(2),
                now.minusDays(1),
                item1,
                booker,
                BookingStatus.WAITING));
        entityManager.persist(makeBooking(null,
                now.plusDays(1),
                now.plusDays(2),
                item2,
                booker,
                BookingStatus.WAITING));

        Pageable pageable = PageRequest.of(0, 20);
        List<Booking> listBookings = bookingRepository.findAllFutureBookingsOwner(owner.getId(),
                LocalDateTime.now(), pageable);

        assertThat(listBookings)
                .hasSize(1)
                .element(0)
                .hasFieldOrProperty("item");
        AssertionsForClassTypes.assertThat(listBookings.get(0).getItem())
                .isInstanceOf(Item.class)
                .hasFieldOrPropertyWithValue("name", "Ultra Ball");
    }
}