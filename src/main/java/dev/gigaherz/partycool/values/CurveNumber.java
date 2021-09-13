package dev.gigaherz.partycool.values;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;

import java.util.List;

public class CurveNumber extends VaryingNumber
{
    public static class Step
    {
        public final float time;
        public final VaryingNumber value;

        public Step(float time, VaryingNumber value)
        {
            this.time = time;
            this.value = value;
        }
    }

    public enum InterpolationMode
    {
        STEP,
        LINEAR,
        CUBIC
    }

    public List<Step> steps = Lists.newArrayList();
    public InterpolationMode interpolationMode = InterpolationMode.LINEAR;

    @Override
    public Codec<? extends VaryingNumber> getCodec()
    {
        return null;
    }

    @Override
    public float getValue(float time)
    {
        float lastStep2 = 0;
        float lastValue2 = 0;
        float lastStep = 0;
        float lastValue = 0;
        for (int i = 0; i < steps.size(); i++)
        {
            Step step = steps.get(i);
            float value = step.value.getValue(time);
            if (time <= step.time)
            {
                return interpolate(lastStep, lastValue, step.time, value, time, i, lastStep2, lastValue2);
            }
            lastStep2 = lastStep;
            lastValue2 = lastValue;
            lastStep = step.time;
            lastValue = value;
        }
        return lastValue;
    }

    private float interpolate(float t0, float v0, float t1, float v1, float time, int i, float tn1, float vn1)
    {
        switch (interpolationMode)
        {
            default:
            case STEP:
                return time >= t1 ? v1 : v0;
            case LINEAR:
                if (t0 >= t1) return v1;
                return inteprolateLinear(t0, v0, t1, v1, time);
            case CUBIC:
                if (i + 1 == steps.size())
                {
                    if (tn1 >= t0)
                    {
                        return inteprolateLinear(t0, v0, t1, v1, time);
                    }
                    else
                    {
                        return interpolateQuadratic(tn1, vn1, t0, v0, t1, v1, time);
                    }
                }
                else
                {
                    Step step = steps.get(i + 1);
                    float t2 = step.time;
                    float v2 = step.value.getValue(time);
                    if (tn1 >= t0)
                    {
                        return interpolateQuadratic(t0, v0, t1, v1, t2, v2, time);
                    }
                    else
                    {
                        return interpolateCubic(tn1, vn1, t0, v0, t1, v1, t2, v2, time);
                    }
                }
        }
    }

    private float inteprolateLinear(float t0, float v0, float t1, float v1, float time)
    {
        float t = (time - t0) / (t1 - t0);
        return v0 + t * (v1 - v0);
    }

    // TODO: find some proper actual code written by someone competent, instead of me
    private float interpolateQuadratic(float t0, float v0, float t1, float v1, float t2, float v2, float time)
    {
        // v0 = A * s0 + B * t0 + C
        // v1 = A * s1 + B * t1 + C
        // v2 = A * s2 + B * t2 + C

        float s0 = t0 * t0;
        float s1 = t1 * t1;
        float s2 = t2 * t2;

        // v2 = A * s2 + B * t2 + C
        // v1 = A * s1 + B * t1 + C
        // v0 = A * s0 + B * t0 + C

        // = dv1 = A * ds1 + B*dt1
        // = dv0 = A * ds0 + B*dt0

        float dv1 = v2 - v1;
        float dv0 = v1 - v0;

        float dt1 = t2 - t1;
        float dt0 = t1 - t0;

        float ds1 = s2 - s1;
        float ds0 = s1 - s0;

        // (dv1 - A * ds1) = B*dt1
        // (dv0 - A * ds0) = B*dt0

        // (dv1 - A * ds1)/dt1 = B
        // (dv0 - A * ds0)/dt0 = B

        float m0 = dv0 / dt0;
        float m1 = dv1 / dt1;
        float n0 = ds0 / dt0;
        float n1 = ds1 / dt1;

        // (dv1 - A * ds1)/dt1 = (dv0 - A * ds0)/dt0
        // m1 - A * n1 = m0 - A * n0
        // m1 - m0 = A * n1 - A * n0
        // m1 - m0 = A * (n1 - n0)

        float A = (m1 - m0) / (n1 - n0);
        float B = (dv1 - A * ds1) / dt1;
        float C = v0 - (A * s0 + B * t0);

        return A * time * time + B * time + C;
    }

    // TODO: find some proper actual code written by someone competent, instead of me
    private float interpolateCubic(float t0, float v0, float t1, float v1, float t2, float v2, float t3, float v3, float time)
    {
        // v0 = A * c0 + B * s0 + C * t0 + D
        // v1 = A * c1 + B * s1 + C * t1 + D
        // v2 = A * c2 + B * s2 + C * t2 + D
        // v3 = A * c3 + B * s3 + C * t3 + D

        // v3 = A * c3 + B * s3 + C * t3 + D
        // v2 = A * c2 + B * s2 + C * t2 + D
        // v1 = A * c1 + B * s1 + C * t1 + D
        // v0 = A * c0 + B * s0 + C * t0 + D

        float s0 = t0 * t0;
        float s1 = t1 * t1;
        float s2 = t2 * t2;
        float s3 = t3 * t3;
        float c0 = s0 * t0;
        float c1 = s1 * t1;
        float c2 = s2 * t2;
        float c3 = s3 * t3;

        float dv2 = v3 - v2;
        float dv1 = v2 - v1;
        float dv0 = v1 - v0;
        float dc2 = c3 - c2;
        float dc1 = c2 - c1;
        float dc0 = c1 - c0;
        float ds2 = s3 - s2;
        float ds1 = s2 - s1;
        float ds0 = s1 - s0;
        float dt2 = t3 - t2;
        float dt1 = t2 - t1;
        float dt0 = t1 - t0;
        // dv2 = A * dc2 + B * ds2 + C * dt2
        // dv1 = A * dc1 + B * ds1 + C * dt1
        // dv0 = A * dc0 + B * ds0 + C * dt0

        // dv2/dt2 = A * dc2/dt2 + B * ds2/dt2 + C
        // dv1/dt1 = A * dc1/dt1 + B * ds1/dt1 + C
        // dv0/dt0 = A * dc0/dt0 + B * ds0/dt0 + C

        float m0 = dv0 / dt0;
        float m1 = dv1 / dt1;
        float m2 = dv2 / dt2;
        float n0 = dc0 / dt0;
        float n1 = dc1 / dt1;
        float n2 = dc2 / dt2;
        float o0 = ds0 / dt0;
        float o1 = ds1 / dt1;
        float o2 = ds2 / dt2;
        // (m2-m1) = A * (n2-n1) + B * (o2-o1)
        // (m1-m0) = A * (n1-n0) + B * (o1-o0)

        float dm1 = m2 - m1;
        float dm0 = m1 - m0;
        float dn1 = n2 - n1;
        float dn0 = n1 - n0;
        float do1 = o2 - o1;
        float do0 = o1 - o0;

        // dm1 = A * dn1 + B * do1
        // dm0 = A * dn0 + B * do0

        // dm1 / do1 = A * dn1 / do1 + B
        // dm0 / do0 = A * dn0 / do0 + B

        // (dm1/do1-dm0/do0) = A * (dn1/do1-dn0/do0)

        float q1 = dm1 / do1;
        float q0 = dm0 / do0;
        float r1 = dn1 / do1;
        float r0 = dn0 / do0;

        float A = (q1 - q0) / (r1 - r0);
        float B = q0 - A * r0;
        float C = m0 - A * n0 - B * o0;
        float D = v0 - A * c0 - B * s0 - C * t0;

        float time2 = time * time;

        return A * time2 * time + B * time2 + C * time + D;
    }
}
