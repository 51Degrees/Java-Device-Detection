/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Performance.Lite;

import Performance.FileBase;
import fiftyone.mobile.detection.entities.Property;
import java.io.IOException;
import org.junit.Test;

/**
 *
 * @author james
 */
public class V32File extends FileBase {
    public V32File() {
        super("../../data/51Degrees-LiteV3.2.dat");
    }
    
    @Override
    protected int getMaxSetupTime() {
        return 500;
    }
    
    @Test
    public void LiteV32File_Performance_InitializeTime()
    {
        super.initializeTime();
    }

    @Test
    public void LiteV32File_Performance_BadUserAgentsMulti() throws IOException
    {
        super.badUserAgentsMulti(null, 1);
    }

    @Test
    public void LiteV32File_Performance_BadUserAgentsSingle() throws IOException
    {
        super.badUserAgentsSingle(null, 6);
    }

    @Test
    public void LiteV32File_Performance_UniqueUserAgentsMulti() throws IOException
    {
        super.uniqueUserAgentsMulti(null, 1);
    }

    @Test
    public void LiteV32File_Performance_UniqueUserAgentsSingle() throws IOException
    {
        super.uniqueUserAgentsSingle(null, 1);
    }

    @Test
    public void LiteV32File_Performance_RandomUserAgentsMulti() throws IOException
    {
        super.randomUserAgentsMulti(null, 1);
    }

    @Test
    public void LiteV32File_Performance_RandomUserAgentsSingle() throws IOException
    {
        super.randomUserAgentsSingle(null, 1);
    }

    @Test
    public void LiteV32File_Performance_BadUserAgentsMultiAll() throws IOException
    {
        super.badUserAgentsMulti(super.dataSet.properties, 1);
    }

    @Test
    public void LiteV32File_Performance_BadUserAgentsSingleAll() throws IOException
    {
        super.badUserAgentsSingle(super.dataSet.properties, 4);
    }

    @Test
    public void LiteV32File_Performance_UniqueUserAgentsMultiAll() throws IOException
    {
        super.uniqueUserAgentsMulti(super.dataSet.properties, 1);
    }

    @Test
    public void LiteV32File_Performance_UniqueUserAgentsSingleAll() throws IOException
    {
        super.uniqueUserAgentsSingle(super.dataSet.properties, 1);
    }

    @Test
    public void LiteV32File_Performance_RandomUserAgentsMultiAll() throws IOException
    {
        super.randomUserAgentsMulti(super.dataSet.properties, 1);
    }

    @Test
    public void LiteV32File_Performance_RandomUserAgentsSingleAll() throws IOException
    {
        super.randomUserAgentsSingle(super.dataSet.properties, 1);
    }
}