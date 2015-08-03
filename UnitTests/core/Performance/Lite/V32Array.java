package Performance.Lite;

import Performance.ArrayBase;
import java.io.IOException;
import org.junit.Test;

/**
 *
 * @author james
 */
public class V32Array extends ArrayBase {
    public V32Array() {
        super("../../data/51Degrees-LiteV3.2.dat");
    }
    
    @Override
    protected int getMaxSetupTime() {
        return 500;
    }
    
    @Test
    public void LiteV32Array_Performance_InitializeTime()
    {
        super.initializeTime();
    }

    @Test
    public void LiteV32Array_Performance_BadUserAgentsMulti() throws IOException
    {
        super.badUserAgentsMulti(null, 1);
    }

    @Test
    public void LiteV32Array_Performance_BadUserAgentsSingle() throws IOException
    {
        super.badUserAgentsSingle(null, 1);
    }

    @Test
    public void LiteV32Array_Performance_UniqueUserAgentsMulti() throws IOException
    {
        super.uniqueUserAgentsMulti(null, 1);
    }

    @Test
    public void LiteV32Array_Performance_UniqueUserAgentsSingle() throws IOException
    {
        super.uniqueUserAgentsSingle(null, 1);
    }

    @Test
    public void LiteV32Array_Performance_RandomUserAgentsMulti() throws IOException
    {
        super.randomUserAgentsMulti(null, 1);
    }

    @Test
    public void LiteV32Array_Performance_RandomUserAgentsSingle() throws IOException
    {
        super.randomUserAgentsSingle(null, 1);
    }

    @Test
    public void LiteV32Array_Performance_BadUserAgentsMultiAll() throws IOException
    {
        super.badUserAgentsMulti(super.dataSet.properties, 1);
    }

    @Test
    public void LiteV32Array_Performance_BadUserAgentsSingleAll() throws IOException
    {
        super.badUserAgentsSingle(super.dataSet.properties, 1);
    }

    @Test
    public void LiteV32Array_Performance_UniqueUserAgentsMultiAll() throws IOException
    {
        super.uniqueUserAgentsMulti(super.dataSet.properties, 1);
    }

    @Test
    public void LiteV32Array_Performance_UniqueUserAgentsSingleAll() throws IOException
    {
        super.uniqueUserAgentsSingle(super.dataSet.properties, 1);
    }

    @Test
    public void LiteV32Array_Performance_RandomUserAgentsMultiAll() throws IOException
    {
        super.randomUserAgentsMulti(super.dataSet.properties, 1);
    }

    @Test
    public void LiteV32Array_Performance_RandomUserAgentsSingleAll() throws IOException
    {
        super.randomUserAgentsSingle(super.dataSet.properties, 1);
    }
}