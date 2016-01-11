package manatee2.prototype.switchsimulator.server;

import java.util.HashMap;
import java.util.Map;

import manatee2.prototype.switchsimulator.shared.SwitchConfiguration;


public class SwitchService
{
    private static final int NUM_INPUT_PORTS = 32;
    private static final int NUM_OUTPUT_PORTS = 64;

    private static Map<Integer, Integer> portMap = new HashMap<Integer, Integer>();


    public SwitchService() throws Exception
    {
        //
        // Start with a random initial mapping.
        //
        portMap.put(2, 5);
        portMap.put(12, 15);
        portMap.put(22, 25);
        portMap.put(32, 35);

        //
        // Simulate a delay.
        //
        try
        {
            Thread.sleep(1500);
        }
        catch (InterruptedException e)
        {
            // Ignore it.
        }
    }


    /**
     * Determine the switch's current configuration.
     * 
     * @return Current Switch Configuration.
     */
    public SwitchConfiguration getConfiguration() throws Exception
    {
        return new SwitchConfiguration(NUM_INPUT_PORTS, NUM_OUTPUT_PORTS, portMap);
    }


    /**
     * Connect an Ingress Port to an Egress Port.
     * 
     * @param inputPort - Ingress Port Number.
     * @param outputPort - Egress Port Number
     * 
     * @return Current Switch Configuration.
     */
    public SwitchConfiguration connect(int inputPort, int outputPort) throws Exception
    {
        //
        // Failsafe.
        //
        if (inputPort < 1 || inputPort > NUM_INPUT_PORTS)
        {
            System.err.println("Unable to connect: Invalid Input-Port " + inputPort);
            return getConfiguration();
        }
        if (outputPort < 1 || outputPort > NUM_OUTPUT_PORTS)
        {
            System.err.println("Unable to connect: Invalid Output-Port " + outputPort);
            return getConfiguration();
        }

        System.out.println("Connecting: " + inputPort + " to " + outputPort);

        //
        // Simulate an error.
        //
        if (inputPort == 4 && outputPort == 20)
        {
            throw new Exception("Dave's not here Man.");
        }

        //
        // Simulate a delay.
        //
        try
        {
            Thread.sleep(1500);
        }
        catch (InterruptedException e)
        {
            // Ignore it.
        }

        //
        // Do the Connect then return the new Switch Configuration.
        //
        portMap.put(inputPort, outputPort);
        return getConfiguration();
    }


    /**
     * Disconnect an Ingress Port from an Egress Port.
     * 
     * @param inputPort - Ingress Port Number.
     * 
     * @return Current Switch Configuration.
     */
    public SwitchConfiguration disconnect(int inputPort) throws Exception
    {
        //
        // Failsafe.
        //
        if (inputPort < 1 || inputPort > NUM_INPUT_PORTS)
        {
            System.err.println("Unable to disconnect: Invalid Input-Port " + inputPort);
            return getConfiguration();
        }

        System.out.println("Disconnecting: " + inputPort + " from " + portMap.get(inputPort));

        //
        // Simulate a delay.
        //
        try
        {
            Thread.sleep(1500);
        }
        catch (InterruptedException e)
        {
            // Ignore it.
        }

        //
        // Do the Disconnect then return the new Switch Configuration.
        //
        portMap.remove(inputPort);
        return getConfiguration();
    }


    /**
     * Disconnect ALL Ingress Ports from Egress Ports.
     * 
     * @return Current Switch Configuration.
     */
    public SwitchConfiguration disconnectAll() throws Exception
    {
        System.out.println("Disconnecting All");

        //
        // Simulate a delay.
        //
        try
        {
            Thread.sleep(1500);
        }
        catch (InterruptedException e)
        {
            // Ignore it.
        }

        //
        // Do the Disconnect then return the new Switch Configuration.
        //
        portMap.clear();
        return getConfiguration();
    }
}
