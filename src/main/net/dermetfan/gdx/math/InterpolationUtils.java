package net.dermetfan.gdx.math;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.ObjectMap;

public final class InterpolationUtils {
    static private final ObjectMap<String, Interpolation> byName = new ObjectMap<String, Interpolation>();

    /**
     * Clears all custom {@code Interpolation}s and resets the underlying map to
     * the default values.
     */
    static public void reset() {
        byName.clear();
        byName.put("bounce", Interpolation.bounce);
        byName.put("bouncein", Interpolation.bounceIn);
        byName.put("bounce-in", Interpolation.bounceIn);
        byName.put("bounce_in", Interpolation.bounceIn);
        byName.put("bounce in", Interpolation.bounceIn);
        byName.put("bounceout", Interpolation.bounceOut);
        byName.put("bounce-out", Interpolation.bounceOut);
        byName.put("bounce_out", Interpolation.bounceOut);
        byName.put("bounce out", Interpolation.bounceOut);

        byName.put("circle", Interpolation.circle);
        byName.put("circlein", Interpolation.circleIn);
        byName.put("circle-in", Interpolation.circleIn);
        byName.put("circle_in", Interpolation.circleIn);
        byName.put("circle in", Interpolation.circleIn);
        byName.put("circleout", Interpolation.circleOut);
        byName.put("circle-out", Interpolation.circleOut);
        byName.put("circle_out", Interpolation.circleOut);
        byName.put("circle out", Interpolation.circleOut);

        byName.put("elastic", Interpolation.elastic);
        byName.put("elasticin", Interpolation.elasticIn);
        byName.put("elastic-in", Interpolation.elasticIn);
        byName.put("elastic_in", Interpolation.elasticIn);
        byName.put("elastic in", Interpolation.elasticIn);
        byName.put("elasticout", Interpolation.elasticOut);
        byName.put("elastic-out", Interpolation.elasticOut);
        byName.put("elastic_out", Interpolation.elasticOut);
        byName.put("elastic out", Interpolation.elasticOut);

        byName.put("exp10", Interpolation.exp10);
        byName.put("exp-10", Interpolation.exp10);
        byName.put("exp_10", Interpolation.exp10);
        byName.put("exp 10", Interpolation.exp10);
        byName.put("exp10in", Interpolation.exp10In);
        byName.put("exp-10-in", Interpolation.exp10In);
        byName.put("exp_10_in", Interpolation.exp10In);
        byName.put("exp 10 in", Interpolation.exp10In);
        byName.put("exp10-in", Interpolation.exp10In);
        byName.put("exp10_in", Interpolation.exp10In);
        byName.put("exp10 in", Interpolation.exp10In);
        byName.put("exp10out", Interpolation.exp10Out);
        byName.put("exp-10-out", Interpolation.exp10Out);
        byName.put("exp_10_out", Interpolation.exp10Out);
        byName.put("exp 10 out", Interpolation.exp10Out);
        byName.put("exp10-out", Interpolation.exp10Out);
        byName.put("exp10_out", Interpolation.exp10Out);
        byName.put("exp10 out", Interpolation.exp10Out);

        byName.put("exp5", Interpolation.exp5);
        byName.put("exp-5", Interpolation.exp5);
        byName.put("exp_5", Interpolation.exp5);
        byName.put("exp 5", Interpolation.exp5);
        byName.put("exp5in", Interpolation.exp5In);
        byName.put("exp-5-in", Interpolation.exp5In);
        byName.put("exp_5_in", Interpolation.exp5In);
        byName.put("exp 5 in", Interpolation.exp5In);
        byName.put("exp5-in", Interpolation.exp5In);
        byName.put("exp5_in", Interpolation.exp5In);
        byName.put("exp5 in", Interpolation.exp5In);
        byName.put("exp5out", Interpolation.exp5Out);
        byName.put("exp-5-out", Interpolation.exp5Out);
        byName.put("exp_5_out", Interpolation.exp5Out);
        byName.put("exp 5 out", Interpolation.exp5Out);
        byName.put("exp5-out", Interpolation.exp5Out);
        byName.put("exp5_out", Interpolation.exp5Out);
        byName.put("exp5 out", Interpolation.exp5Out);

        byName.put("fade", Interpolation.fade);

        byName.put("linear", Interpolation.linear);
        byName.put("line", Interpolation.linear);

        byName.put("quadratic", Interpolation.pow2);
        byName.put("quadratic-in", Interpolation.pow2In);
        byName.put("quadratic_in", Interpolation.pow2In);
        byName.put("quadratic in", Interpolation.pow2In);
        byName.put("quadratic-out", Interpolation.pow2Out);
        byName.put("quadratic_out", Interpolation.pow2Out);
        byName.put("quadratic out", Interpolation.pow2Out);
        byName.put("pow2", Interpolation.pow2);
        byName.put("pow-2", Interpolation.pow2);
        byName.put("pow_2", Interpolation.pow2);
        byName.put("pow 2", Interpolation.pow2);
        byName.put("pow2in", Interpolation.pow2In);
        byName.put("pow-2-in", Interpolation.pow2In);
        byName.put("pow_2_in", Interpolation.pow2In);
        byName.put("pow 2 in", Interpolation.pow2In);
        byName.put("pow2-in", Interpolation.pow2In);
        byName.put("pow2_in", Interpolation.pow2In);
        byName.put("pow2 in", Interpolation.pow2In);
        byName.put("pow2out", Interpolation.pow2Out);
        byName.put("pow-2-out", Interpolation.pow2Out);
        byName.put("pow_2_out", Interpolation.pow2Out);
        byName.put("pow 2 out", Interpolation.pow2Out);
        byName.put("pow2-out", Interpolation.pow2Out);
        byName.put("pow2_out", Interpolation.pow2Out);
        byName.put("pow2 out", Interpolation.pow2Out);

        byName.put("cubic", Interpolation.pow3);
        byName.put("cubic-in", Interpolation.pow3In);
        byName.put("cubic_in", Interpolation.pow3In);
        byName.put("cubic in", Interpolation.pow3In);
        byName.put("cubic-out", Interpolation.pow3Out);
        byName.put("cubic_out", Interpolation.pow3Out);
        byName.put("cubic out", Interpolation.pow3Out);
        byName.put("pow3", Interpolation.pow3);
        byName.put("pow-3", Interpolation.pow3);
        byName.put("pow_3", Interpolation.pow3);
        byName.put("pow 3", Interpolation.pow3);
        byName.put("pow3in", Interpolation.pow3In);
        byName.put("pow-3-in", Interpolation.pow3In);
        byName.put("pow_3_in", Interpolation.pow3In);
        byName.put("pow 3 in", Interpolation.pow3In);
        byName.put("pow3-in", Interpolation.pow3In);
        byName.put("pow3_in", Interpolation.pow3In);
        byName.put("pow3 in", Interpolation.pow3In);
        byName.put("pow3out", Interpolation.pow3Out);
        byName.put("pow-3-out", Interpolation.pow3Out);
        byName.put("pow_3_out", Interpolation.pow3Out);
        byName.put("pow 3 out", Interpolation.pow3Out);
        byName.put("pow3-out", Interpolation.pow3Out);
        byName.put("pow3_out", Interpolation.pow3Out);
        byName.put("pow3 out", Interpolation.pow3Out);

        byName.put("quartic", Interpolation.pow4);
        byName.put("quartic-in", Interpolation.pow4In);
        byName.put("quartic_in", Interpolation.pow4In);
        byName.put("quartic in", Interpolation.pow4In);
        byName.put("quartic-out", Interpolation.pow4Out);
        byName.put("quartic_out", Interpolation.pow4Out);
        byName.put("quartic out", Interpolation.pow4Out);
        byName.put("pow4", Interpolation.pow4);
        byName.put("pow-4", Interpolation.pow4);
        byName.put("pow_4", Interpolation.pow4);
        byName.put("pow 4", Interpolation.pow4);
        byName.put("pow4in", Interpolation.pow4In);
        byName.put("pow-4-in", Interpolation.pow4In);
        byName.put("pow_4_in", Interpolation.pow4In);
        byName.put("pow 4 in", Interpolation.pow4In);
        byName.put("pow4-in", Interpolation.pow4In);
        byName.put("pow4_in", Interpolation.pow4In);
        byName.put("pow4 in", Interpolation.pow4In);
        byName.put("pow4out", Interpolation.pow4Out);
        byName.put("pow-4-out", Interpolation.pow4Out);
        byName.put("pow_4_out", Interpolation.pow4Out);
        byName.put("pow 4 out", Interpolation.pow4Out);
        byName.put("pow4-out", Interpolation.pow4Out);
        byName.put("pow4_out", Interpolation.pow4Out);
        byName.put("pow4 out", Interpolation.pow4Out);

        byName.put("quintic", Interpolation.pow5);
        byName.put("quintic-in", Interpolation.pow5In);
        byName.put("quintic_in", Interpolation.pow5In);
        byName.put("quintic in", Interpolation.pow5In);
        byName.put("quintic-out", Interpolation.pow5Out);
        byName.put("quintic_out", Interpolation.pow5Out);
        byName.put("quintic out", Interpolation.pow5Out);
        byName.put("pow5", Interpolation.pow5);
        byName.put("pow-5", Interpolation.pow5);
        byName.put("pow_5", Interpolation.pow5);
        byName.put("pow 5", Interpolation.pow5);
        byName.put("pow5in", Interpolation.pow5In);
        byName.put("pow-5-in", Interpolation.pow5In);
        byName.put("pow_5_in", Interpolation.pow5In);
        byName.put("pow 5 in", Interpolation.pow5In);
        byName.put("pow5-in", Interpolation.pow5In);
        byName.put("pow5_in", Interpolation.pow5In);
        byName.put("pow5 in", Interpolation.pow5In);
        byName.put("pow5out", Interpolation.pow5Out);
        byName.put("pow-5-out", Interpolation.pow5Out);
        byName.put("pow_5_out", Interpolation.pow5Out);
        byName.put("pow 5 out", Interpolation.pow5Out);
        byName.put("pow5-out", Interpolation.pow5Out);
        byName.put("pow5_out", Interpolation.pow5Out);
        byName.put("pow5 out", Interpolation.pow5Out);

        byName.put("sine", Interpolation.sine);
        byName.put("sinusoidal", Interpolation.sine);
        byName.put("sinein", Interpolation.sineIn);
        byName.put("sine-in", Interpolation.sineIn);
        byName.put("sine_in", Interpolation.sineIn);
        byName.put("sine in", Interpolation.sineIn);
        byName.put("sineout", Interpolation.sineOut);
        byName.put("sine-out", Interpolation.sineOut);
        byName.put("sine_out", Interpolation.sineOut);
        byName.put("sine out", Interpolation.sineOut);

        byName.put("swing", Interpolation.swing);
        byName.put("swingin", Interpolation.swingIn);
        byName.put("swing-in", Interpolation.swingIn);
        byName.put("swing_in", Interpolation.swingIn);
        byName.put("swing in", Interpolation.swingIn);
        byName.put("swingout", Interpolation.swingOut);
        byName.put("swing-out", Interpolation.swingOut);
        byName.put("swing_out", Interpolation.swingOut);
        byName.put("swing out", Interpolation.swingOut);

        byName.shrink(byName.size);
    }

    static {
        reset();
    }

    /**
     * Retrieves one of the default (or even custom, see
     * {@link InterpolationUtils#put}) {@code Interpolation} objects by name.
     * This is mostly for easy specification in configuration files or similar.
     * 
     * The name is case-insensitive, and leading/trailing whitespace is
     * stripped. You can obtain the {@code Interpolation}s via the following
     * conventions, where {@code type} is the main type (e.g.
     * {@link Interpolation#swing}) and {@code sub} is the optional sub-type
     * (e.g. {@link Interpolation#swingIn}), usually either {@code in} or
     * {@code out}.
     * <ul>
     * <li>{@code typesub}
     * <li>{@code type-sub}
     * <li>{@code type_sub}
     * <li>{@code type sub}
     * </ul>
     * 
     * Several synonyms are available, too. The subtype conventions apply.
     * <ul>
     * <li>{@code quadratic} refers to {@link Interpolation#pow2}
     * <li>{@code cubic} refers to {@link Interpolation#pow3}
     * <li>{@code quartic} refers to {@link Interpolation#pow4}
     * <li>{@code quintic} refers to {@link Interpolation#pow5}
     * </ul>
     * 
     * @param name The name of the {@code Interpolation} to retrieve.
     *        Case-insensitive, and leading and trailing whitespace is stripped.
     * @return The {@code Interpolation} if found, {@code null} if not or if
     *         {@code name} is {@code null}.
     */
    static public Interpolation get(final String name) {
        return (name == null) ? null : byName.get(name.toLowerCase().trim(), null);
    }

    /**
     * @param name The name of the new {@code Interpolation} to add in.
     * @param value The {@code Interpolation} itself.
     * @return Whatever {@code Interpolation} was previously referred to by
     *         {@code name}, or {@code null} if there wasn't one.
     */
    static public Interpolation put(String name, Interpolation value) {
        return byName.put(name, value);
    }

    private InterpolationUtils() {

    }
}
