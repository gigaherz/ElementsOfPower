package gigaherz.elementsofpower;

import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class Either<TLeft, TRight>
{
    private final boolean isLeft;
    private final Object value;

    private Either(boolean isLeft, Object value)
    {
        this.isLeft = isLeft;
        this.value = value;
    }

    public static <TLeft, TRight> Either<TLeft, TRight> fromA(TLeft value)
    {
        return new Either(true, value);
    }

    public static <TLeft, TRight> Either<TLeft, TRight> fromB(TRight value)
    {
        return new Either(false, value);
    }

    public boolean isLeft()
    {
        return isLeft;
    }

    public TLeft getLeft()
    {
        if (!isLeft) throw new IllegalStateException("Can not get the Left value on a Right-valued Either");
        return (TLeft) value;
    }

    public TRight getRight()
    {
        if (isLeft) throw new IllegalStateException("Can not get the Right value on a Left-valued Either");
        return (TRight) value;
    }

    public Either<TLeft, TRight> whenLeft(Consumer<TLeft> work)
    {
        if (isLeft) work.accept((TLeft) value);
        return this;
    }

    public Either<TLeft, TRight> whenRight(Consumer<TRight> work)
    {
        if (!isLeft) work.accept((TRight) value);
        return this;
    }
}