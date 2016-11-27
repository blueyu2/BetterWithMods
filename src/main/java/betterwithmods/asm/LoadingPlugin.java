package betterwithmods.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

/**
 * Created by blueyu2 on 11/27/16.
 */
@IFMLLoadingPlugin.MCVersion("1.10.2")
@IFMLLoadingPlugin.TransformerExclusions({ "betterwithmods.asm." })
@IFMLLoadingPlugin.SortingIndex(10001)
@IFMLLoadingPlugin.Name("bwmcore")
public class LoadingPlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[]{BWMTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
