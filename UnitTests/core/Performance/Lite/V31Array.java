/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Performance.Lite;

import Performance.ArrayBase;
import java.io.IOException;
import org.junit.Test;

/**
 *
 * @author james
 */
public class V31Array extends ArrayBase {
    public V31Array() {
        super("../../data/51Degrees-LiteV3.1.dat");
    }
    
    @Override
    protected int getMaxSetupTime() {
        return 500;
    }
    
    @Test
    public void LiteV31Array_Performance_InitializeTime()
    {
        super.initializeTime();
    }

    @Test
    public void LiteV31Array_Performance_BadUserAgentsMulti() throws IOException
    {
        super.badUserAgentsMulti(null, 1);
    }

    @Test
    public void LiteV31Array_Performance_BadUserAgentsSingle() throws IOException
    {
        super.badUserAgentsSingle(null, 3);
    }

    @Test
    public void LiteV31Array_Performance_UniqueUserAgentsMulti() throws IOException
    {
        super.uniqueUserAgentsMulti(null, 1);
    }

    @Test
    public void LiteV31Array_Performance_UniqueUserAgentsSingle() throws IOException
    {
        super.uniqueUserAgentsSingle(null, 1);
    }

    @Test
    public void LiteV31Array_Performance_RandomUserAgentsMulti() throws IOException
    {
        super.randomUserAgentsMulti(null, 1);
    }

    @Test
    public void LiteV31Array_Performance_RandomUserAgentsSingle() throws IOException
    {
        super.randomUserAgentsSingle(null, 1);
    }

    @Test
    public void LiteV31Array_Performance_BadUserAgentsMultiAll() throws IOException
    {
        super.badUserAgentsMulti(super.dataSet.properties, 1);
    }

    @Test
    public void LiteV31Array_Performance_BadUserAgentsSingleAll() throws IOException
    {
        super.badUserAgentsSingle(super.dataSet.properties, 2);
    }

    @Test
    public void LiteV31Array_Performance_UniqueUserAgentsMultiAll() throws IOException
    {
        super.uniqueUserAgentsMulti(super.dataSet.properties, 2);
    }

    @Test
    public void LiteV31Array_Performance_UniqueUserAgentsSingleAll() throws IOException
    {
        super.uniqueUserAgentsSingle(super.dataSet.properties, 1);
    }

    @Test
    public void LiteV31Array_Performance_RandomUserAgentsMultiAll() throws IOException
    {
        super.randomUserAgentsMulti(super.dataSet.properties, 1);
    }

    @Test
    public void LiteV31Array_Performance_RandomUserAgentsSingleAll() throws IOException
    {
        super.randomUserAgentsSingle(super.dataSet.properties, 1);
    }
}