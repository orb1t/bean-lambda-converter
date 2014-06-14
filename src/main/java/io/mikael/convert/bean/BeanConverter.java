package io.mikael.convert.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The primary public face of the converter prototype.
 */
public class BeanConverter<SOURCE, TARGET> {

    private final List<AbstractFieldTransfer<SOURCE, TARGET>> transfers = new LinkedList<>();

    /**
     * Construct a converter from type S to type T.
     */
    public static <S, T> BeanConverter<S, T> fromTo(final Class<S> cs, final Class<T> ct) {
        return new BeanConverter<>();
    }

    /**
     * Construct a converter from type S to type T.
     */
    public static <S, T> BeanConverter<S, T> fromTo() {
        return new BeanConverter<>();
    }

    /**
     * Indicate the transfer of information from a source bean method or lambda,
     * to a target bean method or lambda.
     */
    public <D> BeanConverter<SOURCE, TARGET> field(
            final Function<SOURCE, D> in, final BiConsumer<TARGET, D> out)
    {
        transfers.add(new Field<>(in, out));
        return this;
    }

    /**
     * Indicate that the following field transfers form a group.
     */
    public FieldGroup<SOURCE, TARGET> fieldGroup() {
        final FieldGroup<SOURCE, TARGET> ret = new FieldGroup<>(this);
        transfers.add(ret);
        return ret;
    }

    /**
     * Indicate that the following group of transfers will only be executed,
     * if the {@code isGroupActive} predicate returns {@code true}.
     */
    public FieldGroup<SOURCE, TARGET> fieldGroup(final Predicate<SOURCE> isGroupActive) {
        final FieldGroup<SOURCE, TARGET> ret = new FieldGroup<>(this, isGroupActive);
        transfers.add(ret);
        return ret;
    }

    /**
     * Transfer data from the given source bean instance to the given target bean instance,
     * returning the target bean instance.
     */
    public TARGET convert(final SOURCE source, final TARGET target) {
        transfers.stream()
                .filter(t -> t.isActive(source))
                .forEach(t -> t.transferData(source, target));
        return target;
    }

    /**
     * Transfer data from the given source bean instance to a target bean instance
     * retrieved from the {@code targetSupplier}, returning the target bean instance.
     */
    public TARGET convert(final SOURCE source, final Supplier<TARGET> targetSupplier) {
        return convert(source, targetSupplier.get());
    }

    /**
     * Transfer data from a source bean instance retrieved from the {@code sourceSupplier}
     * to the given target bean instance, returning the target bean instance.
     */
    public TARGET convert(final Supplier<SOURCE> sourceSupplier, final TARGET target) {
        return convert(sourceSupplier.get(), target);
    }

    /**
     * Transfer data from a source bean instance retrieved from the {@code sourceSupplier}
     * to a target bean instance retrieved from the {@code targetSupplier},
     * returning the target bean instance.
     */
    public TARGET convert(final Supplier<SOURCE> sourceSupplier, final Supplier<TARGET> targetSupplier) {
        return convert(sourceSupplier.get(), targetSupplier.get());
    }

}
