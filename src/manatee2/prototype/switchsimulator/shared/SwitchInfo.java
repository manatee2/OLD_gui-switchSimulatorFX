package manatee2.prototype.switchsimulator.shared;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class SwitchConfiguration implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Number of Ingress ports. Must be divisible by 8.
     */
    private int numInputPorts;

    /**
     * Number of Egress ports. Must be divisible by 8.
     */
    private int numOutputPorts;

    /**
     * Mapping of Ingress to Egress Ports.
     * 
     * The Map Keys represent the Ingress Port Number. The Map Values represent the Egress Port Number.
     */
    Map<Integer, Integer> portMap = new HashMap<Integer, Integer>();


    public SwitchConfiguration()
    {
    }


    public SwitchConfiguration(int numInputPorts, int numOutputPorts, Map<Integer, Integer> portMap)
    {
        this.numInputPorts = numInputPorts;
        this.numOutputPorts = numOutputPorts;
        this.portMap = portMap;
    }


    public int getNumInputPorts()
    {
        return numInputPorts;
    }


    public void setNumInputPorts(int numInputPorts)
    {
        this.numInputPorts = numInputPorts;
    }


    public int getNumOutputPorts()
    {
        return numOutputPorts;
    }


    public void setNumOutputPorts(int numOutputPorts)
    {
        this.numOutputPorts = numOutputPorts;
    }


    public Map<Integer, Integer> getPortMap()
    {
        return portMap;
    }


    public void setPortMap(Map<Integer, Integer> portMap)
    {
        this.portMap = portMap;
    }
}
