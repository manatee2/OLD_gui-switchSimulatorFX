package manatee2.prototype.switchsimulator.client;

import javafx.scene.control.Button;


/**
 * A single Switch Button corresponding to an Ingress/Egress Port. Each Ingress Switch Button may be paired with a
 * corresponding Egress Switch Button; and visa-versa.
 */
public class SwitchButton extends Button
{
    /**
     * Label to be placed on the Port (1 to NumPorts).
     */
    private int portNumber;

    /**
     * Corresponding Port to-which this Port is mapped. Null if unmapped.
     */
    private SwitchButton pairedPort;


    SwitchButton(int portNumber)
    {
        this.portNumber = portNumber;
        this.pairedPort = null;
    }


    public int getPortNumber()
    {
        return portNumber;
    }


    public SwitchButton getPairedPort()
    {
        return pairedPort;
    }


    public void setPairedPort(SwitchButton pairedPort)
    {
        this.pairedPort = pairedPort;
    }
}
