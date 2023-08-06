package kaphein.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.junit.jupiter.api.Test;

class NullValueTest
{
    @Test
    void isPresent()
    {
        assertThat(mayBe.isEmpty()).isFalse();
        assertThat(mayBe.isPresent()).isTrue();
    }

    @Test
    void get()
    {
        assertThat(mayBe.get()).isEqualTo(null);
        assertThat(mayBe.get()).isSameAs(null);
    }

    @Test
    void ifPresent()
    {
        final List<String> texts = new LinkedList<>();

        mayBe.ifPresent((value) ->
        {
            texts.add(value);
            texts.add("bar");
        });

        assertThat(texts.size()).isEqualTo(2);
        assertThat(texts.get(0)).isEqualTo(null);
        assertThat(texts.get(1)).isEqualTo("bar");
    }

    @Test
    void ifPresentOrElse()
    {
        final List<String> texts = new LinkedList<>();

        mayBe.ifPresentOrElse(
            (value) ->
            {
                texts.add(value);
                texts.add("bar");
            },
            () -> texts.add("baz")
        );

        assertThat(texts.size()).isEqualTo(2);
        assertThat(texts.get(0)).isEqualTo(null);
        assertThat(texts.get(1)).isEqualTo("bar");
    }

    @Test
    void filter()
    {
        final MayBeValue<String> filteredMayBe1 = mayBe.filter((value) -> "foo".equals(value));

        assertThat(filteredMayBe1.isEmpty()).isTrue();
        assertThrowsExactly(
            NoSuchElementException.class,
            () -> filteredMayBe1.get()
        );

        final MayBeValue<String> filteredMayBe2 = mayBe.filter((value) -> null == value);

        assertThat(filteredMayBe2.isPresent()).isTrue();
        assertThat(filteredMayBe2.get()).isEqualTo(null);
    }

    @Test
    void map()
    {
        final String suffix = "bar";

        final MayBeValue<String> mappedMayBe = mayBe.map((value) -> suffix);

        assertThat(mappedMayBe.isPresent()).isTrue();
        assertThat(mappedMayBe.get()).isEqualTo(suffix);
    }

    @Test
    void flatMap()
    {
        final String suffix = "bar";

        final MayBeValue<String> mappedMayBe = mayBe.flatMap((value) -> MayBeValue.of(suffix));

        assertThat(mappedMayBe.isPresent()).isTrue();
        assertThat(mappedMayBe.get()).isEqualTo(suffix);
    }

    @Test
    void orElse()
    {
        final String text = mayBe.orElse("bar");

        assertThat(text).isEqualTo(null);
    }

    @Test
    void orElseGet()
    {
        final String text = mayBe.orElseGet(String::new);

        assertThat(text).isEqualTo(null);
    }

    @Test
    void orElseThrow()
    {
        final String text = mayBe.orElseThrow(RuntimeException::new);

        assertThat(text).isEqualTo(null);
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    void equality()
    {
        assertThat(mayBe.equals(mayBe)).isTrue();
        assertThat(mayBe.equals(null)).isFalse();
        assertThat(mayBe.equals(new String("foo"))).isFalse();
        assertThat(mayBe.equals(MayBeValue.empty())).isFalse();
        assertThat(mayBe.equals(MayBeValue.of(null))).isTrue();
        assertThat(mayBe.equals(MayBeValue.of("foo"))).isFalse();
        assertThat(mayBe.equals(MayBeValue.of(new String("foo")))).isFalse();
        assertThat(mayBe.equals(MayBeValue.of("bar"))).isFalse();
    }

    @Test
    void hashCodeValue()
    {
        assertThat(mayBe.hashCode()).isNotEqualTo(0);
        assertThat(mayBe.hashCode()).isEqualTo(Objects.hash(true, null));
    }

    @Test
    void toStringValue()
    {
        assertThat(mayBe.toString()).isEqualTo(String.format("%s[%s]", MayBeValue.class.getSimpleName(), "null"));
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

        assertThat(readMayBe.isPresent()).isTrue();
        assertThat(readMayBe.get()).isEqualTo(null);
        assertThat(readMayBe.equals(mayBe)).isTrue();
    }

    private final MayBeValue<String> mayBe = MayBeValue.of(null);
}
