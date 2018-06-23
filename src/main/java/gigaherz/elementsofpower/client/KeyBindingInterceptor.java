package gigaherz.elementsofpower.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;

public class KeyBindingInterceptor extends KeyBinding
{
    protected KeyBinding interceptedKeyBinding;
    private boolean interceptionActive = false;

    /**
     * Create an Interceptor based on an existing binding.
     * The initial interception mode is OFF.
     * If existingKeyBinding is already a KeyBindingInterceptor, a reinitialised copy will be created but no further effect.
     *
     * @param existingKeyBinding - the binding that will be intercepted.
     */
    public KeyBindingInterceptor(KeyBinding existingKeyBinding)
    {
        super(existingKeyBinding.getKeyDescription(), existingKeyBinding.getKeyCode(), existingKeyBinding.getKeyCategory());

        this.interceptedKeyBinding = existingKeyBinding;

        KeyBinding.resetKeyBindingArrayAndHash();
    }

    public void setInterceptionActive(boolean newMode)
    {
        interceptionActive = newMode;
    }

    public boolean isKeyDown()
    {
        return interceptionActive ? false : super.isKeyDown();
    }

    public boolean isKeyDownIntercept()
    {
        return super.isKeyDown();
    }

    @Override
    public boolean isPressed()
    {
        return interceptionActive ? false : super.isPressed();
    }

    public boolean isPressedIntercept()
    {
        return super.isPressed();
    }

    @Override
    public String getKeyCategory()
    {
        return interceptedKeyBinding.getKeyCategory();
    }

    @Override
    public String getKeyDescription()
    {
        return interceptedKeyBinding.getKeyDescription();
    }

    @Override
    public int getKeyCodeDefault()
    {
        return interceptedKeyBinding.getKeyCodeDefault();
    }

    @Override
    public int getKeyCode()
    {
        return interceptedKeyBinding.getKeyCode();
    }

    @Override
    public void setKeyCode(int keyCode)
    {
        interceptedKeyBinding.getKeyCode();
    }

    @Override
    public int compareTo(KeyBinding p_compareTo_1_)
    {
        return interceptedKeyBinding.compareTo(p_compareTo_1_);
    }

    @Override
    public boolean isActiveAndMatches(int keyCode)
    {
        return interceptedKeyBinding.isActiveAndMatches(keyCode);
    }

    @Override
    public void setKeyConflictContext(IKeyConflictContext keyConflictContext)
    {
        interceptedKeyBinding.setKeyConflictContext(keyConflictContext);
    }

    @Override
    public IKeyConflictContext getKeyConflictContext()
    {
        return interceptedKeyBinding.getKeyConflictContext();
    }

    @Override
    public KeyModifier getKeyModifierDefault()
    {
        return interceptedKeyBinding.getKeyModifierDefault();
    }

    @Override
    public KeyModifier getKeyModifier()
    {
        return interceptedKeyBinding != null ? interceptedKeyBinding.getKeyModifier() : KeyModifier.NONE;
    }

    @Override
    public void setKeyModifierAndCode(KeyModifier keyModifier, int keyCode)
    {
        interceptedKeyBinding.setKeyModifierAndCode(keyModifier, keyCode);
    }

    @Override
    public void setToDefault()
    {
        interceptedKeyBinding.setToDefault();
    }

    @Override
    public boolean isSetToDefaultValue()
    {
        return interceptedKeyBinding.isSetToDefaultValue();
    }

    @Override
    public boolean conflicts(KeyBinding other)
    {
        return interceptedKeyBinding.conflicts(other);
    }

    @Override
    public boolean hasKeyCodeModifierConflict(KeyBinding other)
    {
        return interceptedKeyBinding.hasKeyCodeModifierConflict(other);
    }

    @Override
    public String getDisplayName()
    {
        return interceptedKeyBinding.getDisplayName();
    }
}
