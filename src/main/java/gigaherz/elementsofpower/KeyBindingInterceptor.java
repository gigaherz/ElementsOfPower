package gigaherz.elementsofpower;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.util.List;

@SideOnly(Side.CLIENT)
public class KeyBindingInterceptor extends KeyBinding
{
    static List keybindArray = null;

    protected KeyBinding interceptedKeyBinding;
    private boolean interceptionActive;

    private int interceptedPressTime;

    /**
     *  Create an Interceptor based on an existing binding.
     *  The initial interception mode is OFF.
     *  If existingKeyBinding is already a KeyBindingInterceptor, a reinitialised copy will be created but no further effect.
     * @param existingKeyBinding - the binding that will be intercepted.
     */
    public KeyBindingInterceptor(KeyBinding existingKeyBinding)
    {
        super(existingKeyBinding.getKeyDescription(), existingKeyBinding.getKeyCode(), existingKeyBinding.getKeyCategory());

        // the base constructor automatically adds the class to the keybindArray and hash, which we don't want, so undo it
        if(keybindArray == null) {
            getKeybindArrayFromSuper();
        }
        keybindArray.remove(this);

        this.interceptionActive = false;

        setPressedAndTime(this, false, 0);

        this.interceptedPressTime = 0;

        if (existingKeyBinding instanceof KeyBindingInterceptor) {
            interceptedKeyBinding = ((KeyBindingInterceptor)existingKeyBinding).getOriginalKeyBinding();
        } else {
            interceptedKeyBinding = existingKeyBinding;
        }

        KeyBinding.resetKeyBindingArrayAndHash();
    }

    private void getKeybindArrayFromSuper() {
        try {
            Field fPressed = KeyBinding.class.getDeclaredField("keybindArray");
            fPressed.setAccessible(true);
            keybindArray = (List)fPressed.get(null);

        } catch(NoSuchFieldException e) {
            e.printStackTrace();
        } catch(IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static void setPressedAndTime(KeyBinding binding, boolean pressed, int time) {
        try {
            Field fPressed = KeyBinding.class.getDeclaredField("pressed");
            fPressed.setAccessible(true);
            fPressed.set(binding, pressed);

            Field fPressTime = KeyBinding.class.getDeclaredField("pressTime");
            fPressTime.setAccessible(true);
            fPressTime.set(binding, time);

        } catch(NoSuchFieldException e) {
            e.printStackTrace();
        } catch(IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static void setPressTime(KeyBinding binding, int time) {
        try {
            Field fPressTime = KeyBinding.class.getDeclaredField("pressTime");
            fPressTime.setAccessible(true);
            fPressTime.set(binding, time);

        } catch(NoSuchFieldException e) {
            e.printStackTrace();
        } catch(IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static boolean getPressed(KeyBinding binding) {
        try {
            Field fPressed = KeyBinding.class.getDeclaredField("pressed");
            fPressed.setAccessible(true);
            return (Boolean)fPressed.get(binding);

        } catch(NoSuchFieldException e) {
            e.printStackTrace();
        } catch(IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static int getPressTime(KeyBinding binding) {
        try {
            Field fPressTime = KeyBinding.class.getDeclaredField("pressTime");
            fPressTime.setAccessible(true);
            return (Integer)fPressTime.get(binding);

        } catch(NoSuchFieldException e) {
            e.printStackTrace();
        } catch(IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setInterceptionActive(boolean newMode)
    {
        if (newMode && !interceptionActive) {
            this.interceptedPressTime = 0;
        }
        interceptionActive = newMode;
    }

    public boolean isKeyDown()
    {
        copyKeyCodeToOriginal();
        return interceptedKeyBinding.isPressed();
    }

    /**
     *
     * @return returns false if interception isn't active.  Otherwise, retrieves one of the clicks (true) or false if no clicks left
     */
    public boolean retrieveClick()
    {
        copyKeyCodeToOriginal();
        if (interceptionActive) {
            copyClickInfoFromOriginal();

            if (this.interceptedPressTime == 0) {
                return false;
            } else {
                --this.interceptedPressTime;
                return true;
            }
        } else {
            return false;
        }
    }

    /** A better name for this method would be retrieveClick.
     * If interception is on, resets .pressed and .pressTime to zero.
     * Otherwise, copies these from the intercepted KeyBinding.
     * @return If interception is on, this will return false; Otherwise, it will pass on any clicks in the intercepted KeyBinding
     */
    @Override
    public boolean isPressed()
    {
        copyKeyCodeToOriginal();
        copyClickInfoFromOriginal();

        if (interceptionActive) {
            setPressedAndTime(this, false, 0);
            return false;
        } else {
            int pressTime = getPressTime(this);
            if (pressTime == 0) {
                return false;
            } else {
                setPressTime(this, --pressTime);
                return true;
            }
        }
    }

    public KeyBinding getOriginalKeyBinding() {
        return interceptedKeyBinding;
    }

    protected void copyClickInfoFromOriginal()
    {
        int pressTimeIntercepted = getPressTime(interceptedKeyBinding);
        int pressTime = getPressTime(this) + pressTimeIntercepted;
        this.interceptedPressTime += pressTimeIntercepted;
        setPressTime(interceptedKeyBinding, 0);
        setPressedAndTime(this, getPressed(interceptedKeyBinding), pressTime);
    }

    protected void copyKeyCodeToOriginal()
    {
        if (this.getKeyCode() != interceptedKeyBinding.getKeyCode()) {
            setKeyCode(interceptedKeyBinding.getKeyCode());
            resetKeyBindingArrayAndHash();
        }
    }

}
