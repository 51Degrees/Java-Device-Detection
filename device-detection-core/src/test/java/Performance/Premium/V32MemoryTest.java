package Performance.Premium;

import Performance.MemoryBase;
import Properties.Constants;
import java.io.IOException;
import org.junit.Test;

/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 * 
 * This Source Code Form is the subject of the following patent 
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY: 
 * European Patent Application No. 13192291.6; and
 * United States Patent Application Nos. 14/085,223 and 14/085,301.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0.
 * 
 * If a copy of the MPL was not distributed with this file, You can obtain
 * one at http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */

public class V32MemoryTest extends MemoryBase {
    public V32MemoryTest() {
        super(Constants.PREMIUM_PATTERN_V32);
    }
    
    @Override
    protected int getMaxSetupTime() {
        return 10000;
    }
    
    @Test
    public void PremiumV32Memory_Performance_InitializeTime()
    {
        super.initializeTime();
    }

    @Test
    public void PremiumV32Memory_Performance_BadUserAgentsMulti() throws IOException
    {
        super.badUserAgentsMulti(null, 1);
    }

    @Test
    public void PremiumV32Memory_Performance_BadUserAgentsSingle() throws IOException
    {
        super.badUserAgentsSingle(null, 2);
    }

    @Test
    public void PremiumV32Memory_Performance_UniqueUserAgentsMulti() throws IOException
    {
        super.uniqueUserAgentsMulti(null, 1);
    }

    @Test
    public void PremiumV32Memory_Performance_UniqueUserAgentsSingle() throws IOException
    {
        super.uniqueUserAgentsSingle(null, 1);
    }

    @Test
    public void PremiumV32Memory_Performance_RandomUserAgentsMulti() throws IOException
    {
        super.randomUserAgentsMulti(null, 1);
    }

    @Test
    public void PremiumV32Memory_Performance_RandomUserAgentsSingle() throws IOException
    {
        super.randomUserAgentsSingle(null, 1);
    }

    @Test
    public void PremiumV32Memory_Performance_BadUserAgentsMultiAll() throws IOException
    {
        super.badUserAgentsMulti(super.dataSet.properties, 1);
    }

    @Test
    public void PremiumV32Memory_Performance_BadUserAgentsSingleAll() throws IOException
    {
        super.badUserAgentsSingle(super.dataSet.properties, 2);
    }

    @Test
    public void PremiumV32Memory_Performance_UniqueUserAgentsMultiAll() throws IOException
    {
        super.uniqueUserAgentsMulti(super.dataSet.properties, 1);
    }

    @Test
    public void PremiumV32Memory_Performance_UniqueUserAgentsSingleAll() throws IOException
    {
        super.uniqueUserAgentsSingle(super.dataSet.properties, 1);
    }

    @Test
    public void PremiumV32Memory_Performance_RandomUserAgentsMultiAll() throws IOException
    {
        super.randomUserAgentsMulti(super.dataSet.properties, 1);
    }

    @Test
    public void PremiumV32Memory_Performance_RandomUserAgentsSingleAll() throws IOException
    {
        super.randomUserAgentsSingle(super.dataSet.properties, 1);
    }
}