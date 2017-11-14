package org.assertj.vavr.api;

/*
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  specific language governing permissions and limitations under the License.
  <p>
  Copyright 2012-2017 the original author or authors.
 */

import io.vavr.control.Try;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.assertj.core.internal.ComparatorBasedComparisonStrategy;
import org.assertj.core.internal.ComparisonStrategy;
import org.assertj.core.internal.Conditions;
import org.assertj.core.internal.FieldByFieldComparator;
import org.assertj.core.internal.StandardComparisonStrategy;
import org.assertj.core.util.CheckReturnValue;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.util.Preconditions.checkArgument;
import static org.assertj.vavr.api.TryShouldBeSuccess.shouldBeSuccess;
import static org.assertj.vavr.api.TryShouldContain.shouldContain;
import static org.assertj.vavr.api.TryShouldContain.shouldContainSame;
import static org.assertj.vavr.api.TryShouldContainInstanceOf.shouldContainInstanceOf;

/**
 * Assertions for {@link io.vavr.control.Try}.
 *
 * @param <SELF>  the "self" type of this assertion class.
 * @param <VALUE> type of the value contained in the {@link io.vavr.control.Try}.
 * @author Grzegorz Piwowarek
 */
abstract class AbstractTryAssert<SELF extends AbstractTryAssert<SELF, VALUE>, VALUE> extends
  AbstractAssert<SELF, Try<VALUE>> {

    private Conditions conditions = Conditions.instance();

    private ComparisonStrategy TryValueComparisonStrategy;

    AbstractTryAssert(Try<VALUE> actual, Class<?> selfType) {
        super(actual, selfType);
        this.TryValueComparisonStrategy = StandardComparisonStrategy.instance();
    }

    /**
     * Verifies that the actual {@link io.vavr.control.Try} contains the given value.
     *
     * @param expectedValue the expected value inside the {@link io.vavr.control.Try}.
     * @return this assertion object.
     */
    public SELF contains(VALUE expectedValue) {
        isNotNull();
        checkNotNull(expectedValue);
        if (actual.isEmpty()) throwAssertionError(shouldContain(expectedValue));
        if (!TryValueComparisonStrategy.areEqual(actual.get(), expectedValue))
            throwAssertionError(shouldContain(actual, expectedValue));
        return myself;
    }

    /**
     * Verifies that the actual {@link io.vavr.control.Try} contains a value and gives this value to the given
     * {@link java.util.function.Consumer} for further assertions. Should be used as a way of deeper asserting on the
     * containing object, as further requirement(s) for the value.
     *
     * @param requirement to further assert on the object contained inside the {@link io.vavr.control.Try}.
     * @return this assertion object.
     */
    public SELF hasValueSatisfying(Consumer<VALUE> requirement) {
        assertValueIsPresent();
        requirement.accept(actual.get());
        return myself;
    }

    /**
     * Verifies that the actual {@link Try} contains a value which satisfies the given {@link Condition}.
     *
     * @param condition the given condition.
     * @return this assertion object.
     * @throws AssertionError       if the actual {@link Try} is null or empty.
     * @throws NullPointerException if the given condition is {@code null}.
     * @throws AssertionError       if the actual value does not satisfy the given condition.
     */
    public SELF hasValueSatisfying(Condition<? super VALUE> condition) {
        assertValueIsPresent();
        conditions.assertIs(info, actual.get(), condition);
        return myself;
    }

    /**
     * Verifies that the actual {@link Try} contains a value that is an instance of the argument.
     *
     * @param clazz the expected class of the value inside the {@link Try}.
     * @return this assertion object.
     */
    public SELF containsInstanceOf(Class<?> clazz) {
        assertValueIsPresent();
        if (!clazz.isInstance(actual.get())) throwAssertionError(shouldContainInstanceOf(actual, clazz));
        return myself;
    }

    /**
     * Use field/property by field/property comparison (including inherited fields/properties) instead of relying on
     * actual type A <code>equals</code> method to compare the {@link Try} value's object for incoming assertion
     * checks. Private fields are included but this can be disabled using
     * {@link Assertions#setAllowExtractingPrivateFields(boolean)}.
     *
     * @return {@code this} assertion object.
     */
    @CheckReturnValue
    public SELF usingFieldByFieldValueComparator() {
        return usingValueComparator(new FieldByFieldComparator());
    }

    /**
     * Use given custom comparator instead of relying on actual type A <code>equals</code> method to compare the
     * {@link Try} value's object for incoming assertion checks.
     *
     * @param customComparator the comparator to use for incoming assertion checks.
     * @return {@code this} assertion object.
     * @throws NullPointerException if the given comparator is {@code null}.
     */
    @CheckReturnValue
    public SELF usingValueComparator(Comparator<? super VALUE> customComparator) {
        TryValueComparisonStrategy = new ComparatorBasedComparisonStrategy(customComparator);
        return myself;
    }

    /**
     * Revert to standard comparison for incoming assertion {@link Try} value checks.
     * <p>
     * This method should be used to disable a custom comparison strategy set by calling
     * {@link #usingValueComparator(Comparator)}.
     *
     * @return {@code this} assertion object.
     */
    @CheckReturnValue
    public SELF usingDefaultValueComparator() {
        // fall back to default strategy to compare actual with other objects.
        TryValueComparisonStrategy = StandardComparisonStrategy.instance();
        return myself;
    }

    /**
     * Verifies that the actual {@link io.vavr.control.Try} contains the instance given as an argument (i.e. it must be the
     *
     * @param expectedValue the expected value inside the {@link io.vavr.control.Try}.
     * @return this assertion object.
     */
    public SELF containsSame(VALUE expectedValue) {
        isNotNull();
        checkNotNull(expectedValue);
        if (actual.isEmpty()) throwAssertionError(shouldContain(expectedValue));
        if (actual.get() != expectedValue) throwAssertionError(shouldContainSame(actual, expectedValue));
        return myself;
    }

    /**
     * Call {@link Try#flatMap(Function) flatMap} on the {@code Try} under test, assertions chained afterwards are performed on the {@code Try} resulting from the flatMap call.
     *
     * @param mapper the {@link Function} to use in the {@link Try#flatMap(Function) flatMap} operation.
     * @return a new {@link org.assertj.vavr.api.AbstractTryAssert} for assertions chaining on the flatMap of the Try.
     * @throws AssertionError if the actual {@link Try} is null.
     */
    @CheckReturnValue
    public <U> AbstractTryAssert<?, U> flatMap(Function<? super VALUE, Try<U>> mapper) {
        isNotNull();
        return VavrAssertions.assertThat(actual.flatMap(mapper));
    }

    /**
     * Call {@link Try#map(Function) map} on the {@code Try} under test, assertions chained afterwards are performed on the {@code Try} resulting from the map call.
     *
     * @param mapper the {@link Function} to use in the {@link Try#map(Function) map} operation.
     * @return a new {@link org.assertj.vavr.api.AbstractTryAssert} for assertions chaining on the map of the Try.
     * @throws AssertionError if the actual {@link Try} is null.
     */
    @CheckReturnValue
    public <U> AbstractTryAssert<?, U> map(Function<? super VALUE, ? extends U> mapper) {
        isNotNull();
        return VavrAssertions.assertThat(actual.map(mapper));
    }

    private void checkNotNull(Object expectedValue) {
        checkArgument(expectedValue != null, "The expected value should not be <null>.");
    }

    private void assertValueIsPresent() {
        isNotNull();
        if (actual.isEmpty()) throwAssertionError(shouldBeSuccess());
    }
}
