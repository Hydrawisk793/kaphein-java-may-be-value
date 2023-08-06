package kaphein.util;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 *  A container object which may or may not contain a {@code null}able value.
 *
 *  <p>Unlike {@link java.util.Optional}, the value described by a {@link MayBeValue} instance can be {@code null}.
 *  An empty {@link MayBeValue} instance represents that there is NEITHER non-{@code null} value nor {@code null} in the {@link MayBeValue} instance.
 *
 *  <p>{@link MayBeValue} is {@link java.io.Serializable}.
 *  A {@code boolean} flag which represents whether a value is present or not comes first.
 *  If a value is present, the value comes after the flag otherwise {@code null} comes after the flag.
 *
 *  <p>{@link MayBeValue} is intended to represent optional fields in JSON schemas or JavaScript/TypeScript object literals.
 *  So, {@link MayBeValue} can be used as a field or parameter, whereas {@link java.util.Optional} can't.
 *
 *  @author Hydrawisk793
 */
public final class MayBeValue<T> implements Serializable
{
    /**
     *  Returns an empty {@link MayBeValue} instance. No value is present.
     *
     *  @param <T> The type of the non-existent value
     *  @return An empty {@link MayBeValue}
     */
    @SuppressWarnings("unchecked")
    public static <T> MayBeValue<T> empty()
    {
        return (MayBeValue<T>)EMPTY;
    }

    /**
     *  Returns an {@link MayBeValue} describing the given value. The value can be {@code null}.
     *
     *  @param <T> The type of the value
     *  @param value The value to describe, which can be {@code null}
     *  @return An {@link MayBeValue} with the value present
     */
    public static <T> MayBeValue<T> of(T value)
    {
        return new MayBeValue<T>(true, value);
    }

    /**
     *  If a value is present, returns {@code true}, otherwise {@code false}.
     *
     *  @return {@code true} if a value is present, otherwise {@code false}
     */
    public boolean isPresent()
    {
        return present;
    }

    /**
     *  If a value is not present, returns {@code true}, otherwise {@code false}.
     *
     *  @return {@code true} if a value is not present, otherwise {@code false}
     */
    public boolean isEmpty()
    {
        return !present;
    }

    /**
     *  If a value is present, returns the value, otherwise throws {@link NoSuchElementException}.
     *
     *  @return The value described by this {@link MayBeValue}
     *  @throws NoSuchElementException If no value is present
     */
    public T get()
    {
        if(!present)
        {
            throw new NoSuchElementException("No value is present");
        }

        return value;
    }

    /**
     *  If a value is present, performs the given action with the value, otherwise does nothing.
     *
     *  @param action The action to be performed, if a value is present
     */
    public void ifPresent(Consumer<? super T> action)
    {
        if(present)
        {
            Objects.requireNonNull(action).accept(value);
        }
    }

    /**
     *  If a value is present, performs the given action with the value, otherwise performs the given empty-based action.
     *
     *  @param action The action to be performed, if a value is present
     *  @param emptyAction The empty-based action to be performed, if no value is present
     */
    public void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction)
    {
        if(present)
        {
            Objects.requireNonNull(action).accept(value);
        }
        else
        {
            Objects.requireNonNull(emptyAction).run();
        }
    }

    /**
     *  If a value is present, and the value matches the given predicate,
     *  returns an {@link MayBeValue} describing the value,
     *  otherwise returns an empty {@link MayBeValue}.
     *
     *  @param predicate The predicate to apply to a value, if present
     *  @return An {@link MayBeValue} describing the value of this {@link MayBeValue},
     *  if a value is present and the value matches the given predicate,
     *  otherwise an empty {@link MayBeValue}
     */
    public MayBeValue<T> filter(Predicate<? super T> predicate)
    {
        return (present && predicate.test(value) ? of(value) : empty());
    }

    /**
     *  If a value is present, apply the provided mapping function to it and return an {@link MayBeValue} describing the result.
     *  Otherwise return an empty {@link MayBeValue}.
     *
     *  @param <U> The type of the result of the mapping function
     *  @param mapper The mapping function to apply to a value, if present
     *  @return An {@link MayBeValue} describing the result of applying a mapping function to the value of this {@link MayBeValue}, if a value is present,
     *  otherwise an empty {@link MayBeValue}
     */
    public <U> MayBeValue<U> map(Function<? super T,? extends U> mapper)
    {
        return (present ? of(mapper.apply(value)) : empty());
    }

    /**
     *  If a value is present, apply the provided {@link MayBeValue}-bearing mapping function to it, return that result,
     *  otherwise return an empty {@link MayBeValue}.
     *  This method is similar to {@link MayBeValue#map(Function)}, but the provided mapper is one whose result is already an {@link MayBeValue},
     *  and if invoked, {@link MayBeValue#flatMap} does not wrap it with an additional {@link MayBeValue}.
     *
     *  @param <U> The type parameter to the {@link MayBeValue} returned by
     *  @param mapper The mapping function to apply to a value, if present
     *  @return The result of applying an {@link MayBeValue}-bearing mapping function to the value of this {@link MayBeValue}, if a value is present,
     *  otherwise an empty {@link MayBeValue}
     */
    public <U> MayBeValue<U> flatMap(Function<? super T, MayBeValue<U>> mapper)
    {
        return (present ? mapper.apply(value) : empty());
    }

    /**
     *  Return the value if present, otherwise return other.
     *
     *  @param other The value to be returned if there is no value present, may be {@code null}
     *  @return The value, if present, otherwise other
     */
    public T orElse(T other)
    {
        return (present ? value : other);
    }

    /**
     *  Return the value if present, otherwise invoke other and return the result of that invocation.
     *
     *  @param other A {@link Supplier} whose result is returned if no value is present
     *  @return The value if present otherwise the result of {@code other.get()}
     */
    public T orElseGet(Supplier<? extends T> other)
    {
        return (present ? value : other.get());
    }

    /**
     *  Return the contained value, if present, otherwise throw an exception to be created by the provided supplier.
     *
     *  @param <X> Type of the exception to be thrown
     *  @param exceptionSupplier The supplying function that produces an exception to be thrown
     *  @return The value described by this {@link MayBeValue}
     *  @throws NullPointerException If no value is present and exceptionSupplier is {@code null}
     *  @throws X If no value is present
     */
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X
    {
        if(!present)
        {
            throw Objects.requireNonNull(exceptionSupplier).get();
        }

        return value;
    }

    /**
     *  The other object is considered equal if:
     *  <ul>
     *  <li>it is also an {@link MayBeValue} and;</li>
     *  <li>both instances have no value present or;</li>
     *  <li>the present values are "equal to" each other via {@code equals()}.</li>
     *  </ul>
     *
     *  @param obj An object to be tested
     *  @return {@code true} if both are equal otherwise {@code false}
     */
    @Override
    public boolean equals(Object obj)
    {
        boolean result = this == obj;

        if(!result)
        {
            result = null != obj;

            if(result)
            {
                result = getClass() == obj.getClass();

                if(result)
                {
                    MayBeValue<?> other = (MayBeValue<?>)obj;

                    result = present == other.present
                        && Objects.equals(value, other.value);
                }
            }
        }

        return result;
    }

    /**
     *  Returns the hash code value of the present value, if any, or 0 (zero) if no value is present.
     *
     *  @return Hash code value of the present value or 0 if no value is present
     */
    @Override
    public int hashCode()
    {
        return (present ? Objects.hash(present, value) : 0);
    }

    /**
     *  Returns a non-empty string representation of this {@code MayBeValue}
     *
     *  @return The string representation of this instance
     */
    @Override
    public String toString()
    {
        return "MayBeValue" + (present ? "[" + (null == value ? "null" : value.toString()) + "]" : ".empty");
    }

    private static final long serialVersionUID = -3857307535978517210L;

    /**
     *  The empty {@link MayBeValue} instance
     */
    private static final MayBeValue<?> EMPTY = new MayBeValue<>();

    /**
     *  Constructs an empty instance.
     */
    private MayBeValue()
    {
        this(false, null);
    }

    /**
     *  Constructs an empty instance with all field value arguments.
     */
    private MayBeValue(boolean present, T value)
    {
        this.present = present;
        this.value = value;
    }

    /**
     *  Deserializes a {@link MayBeValue} instance from the specified {@link ObjectOutputStream}.
     *
     *  @param ois An {@link ObjectInputStream} to read
     *  @throws IOException If any I/O exception occurred
     *  @throws ClassNotFoundException The class for the value has not been found
     */
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
    {
        Objects.requireNonNull(ois);

        ois.defaultReadObject();

        present = ois.readBoolean();
        value = (T)ois.readObject();

        if(!present && !equals(EMPTY))
        {
            throw new InvalidObjectException("Malformed object format");
        }
    }

   /**
    *  Serializes this instance to the specified {@link ObjectOutputStream}.
    *
    *  @param ows The destination {@link ObjectOutputStream}
    *  @throws IOException If any I/O exception occurred
    */
    private void writeObject(ObjectOutputStream ows) throws IOException
    {
        Objects.requireNonNull(ows);

        ows.defaultWriteObject();

        ows.writeBoolean(present);
        ows.writeObject(value);
    }

    /**
     *  A flag that represents whether a value is present or not.
     */
    private boolean present;

    /**
     *  The value described by this instance.
     */
    private T value;
}
