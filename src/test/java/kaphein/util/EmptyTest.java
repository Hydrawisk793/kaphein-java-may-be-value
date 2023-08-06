package kaphein.util;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class EmptyTest
{
    @Test
    void isPresent()
    {
        assertThat(mayBe.isEmpty()).isTrue();
        assertThat(mayBe.isPresent()).isFalse();
    }

    @Test
    void get()
    {
        assertThatThrownBy(mayBe::get).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void ifPresent()
    {
        final List<String> texts = new LinkedList<>();

        mayBe.ifPresent((value) -> texts.add("bar"));

        assertThat(texts.isEmpty()).isTrue();
    }

    @Test
    void ifPresentOrElse()
    {
        final List<String> texts = new LinkedList<>();

        mayBe.ifPresentOrElse(
            (value) -> texts.add("foo"),
            () -> texts.add("bar")
        );

        assertThat(texts.isEmpty()).isFalse();
        assertThat(texts.get(0)).isEqualTo("bar");
    }

    @Test
    void filter()
    {
        final MayBeValue<String> filteredMayBe = mayBe.filter((value) -> "foo".equals(value));

        assertThat(filteredMayBe.isEmpty()).isTrue();
    }

    @Test
    void map()
    {
        final String suffix = "bar";

        final MayBeValue<String> mappedMayBe = mayBe.map((value) -> value + suffix);

        assertThat(mappedMayBe.isEmpty()).isTrue();
        assertThat(mappedMayBe.equals(mayBe)).isTrue();
    }

    @Test
    void flatMap()
    {
        final String suffix = "bar";

        final MayBeValue<String> mappedMayBe = mayBe.flatMap((value) -> MayBeValue.of(value + suffix));

        assertThat(mappedMayBe.isEmpty()).isTrue();
    }

    @Test
    void orElse()
    {
        final String text = mayBe.orElse("bar");

        assertThat(text).isEqualTo("bar");
    }

    @Test
    void orElseGet()
    {
        final String text = mayBe.orElseGet(String::new);

        assertThat(text).isEmpty();
        assertThat(text).isNotSameAs("");
    }

    @Test
    void orElseThrow()
    {
        assertThrowsExactly(
            RuntimeException.class,
            () -> mayBe.orElseThrow(RuntimeException::new)
        );
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    void equality()
    {
        assertThat(mayBe.equals(mayBe)).isTrue();
        assertThat(mayBe.equals(null)).isFalse();
        assertThat(mayBe.equals(new String("foo"))).isFalse();
        assertThat(mayBe.equals(MayBeValue.empty())).isTrue();
        assertThat(mayBe.equals(MayBeValue.of(null))).isFalse();
        assertThat(mayBe.equals(MayBeValue.of("foo"))).isFalse();
        assertThat(mayBe.equals(MayBeValue.of(new String("foo")))).isFalse();
        assertThat(mayBe.equals(MayBeValue.of("bar"))).isFalse();
    }

    @Test
    void hashCodeValue()
    {
        assertThat(mayBe.hashCode()).isEqualTo(0);
    }

    @Test
    void toStringValue()
    {
        assertThat(mayBe.toString()).isEqualTo(MayBeValue.class.getSimpleName() + ".empty");
    }

    @Test
    void writeAndReadBack() throws IOException, ClassNotFoundException
    {
        byte[] serializedMayBe = null;
        try(
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
        )
        {
            oos.writeObject(mayBe);
            serializedMayBe = baos.toByteArray();
        }

        Object readObject = null;
        try(ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serializedMayBe)))
        {
            readObject = ois.readObject();
        }

        assertThat(readObject.getClass()).isSameAs(MayBeValue.class);

        @SuppressWarnings("unchecked")
        final MayBeValue<String> readMayBe = (MayBeValue<String>)readObject;

        assertThat(readMayBe.isEmpty()).isTrue();
        assertThrowsExactly(
            NoSuchElementException.class,
            () -> readMayBe.get()
        );
        assertThat(readMayBe.equals(MayBeValue.empty())).isTrue();
    }

    @Test
    void readMalformedEmptyObject() throws IOException, ClassNotFoundException
    {
        final byte[] malformedEnptyMayBe = new byte[]
        {
            (byte)0xAC, (byte)0xED, 0x00, 0x05, 0x73, 0x72, 0x00, 0x17,
            0x6B, 0x61, 0x70, 0x68, 0x65, 0x69, 0x6E, 0x2E,
            0x75, 0x74, 0x69, 0x6C, 0x2E, 0x4D, 0x61, 0x79,
            0x42, 0x65, 0x56, 0x61, 0x6C, 0x75, 0x65,
            // serialVersionUID
            (byte)0xCA, 0x78, 0x17, 0x3B, (byte)0xA0, (byte)0x90, 0x2D, 0x26,
            0x03, 0x00, 0x02, 0x5A, 0x00, 0x07, 0x70, 0x72, 0x65,
            0x73, 0x65, 0x6E, 0x74, 0x4C, 0x00, 0x05, 0x76,
            0x61, 0x6C, 0x75, 0x65, 0x74, 0x00, 0x12, 0x4C,
            0x6A, 0x61, 0x76, 0x61, 0x2F, 0x6C, 0x61, 0x6E,
            0x67, 0x2F, 0x4F, 0x62, 0x6A, 0x65, 0x63, 0x74,
            0x3B, 0x78, 0x70, 0x00, 0x74, 0x00, 0x03, 0x66,
            0x6F, 0x6F, 0x77, 0x01, 0x00, 0x71, 0x00, 0x7E,
            0x00, 0x03, 0x78
        };

        try(final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(malformedEnptyMayBe)))
        {
            assertThrowsExactly(
                InvalidObjectException.class,
                () -> ois.readObject()
            );
        }
    }

    private final MayBeValue<String> mayBe = MayBeValue.empty();
}
