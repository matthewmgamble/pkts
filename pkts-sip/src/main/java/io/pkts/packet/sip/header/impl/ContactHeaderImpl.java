/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.header.ContactHeader;
import java.util.List;


/**
 * @author jonas@jonasborjesson.com
 */
public class ContactHeaderImpl extends MultiAddressParametersHeaderImpl implements ContactHeader {

    public ContactHeaderImpl(Buffer name, final List<AddressEntry> address) {
        super(name, address);
    }

    @Override
    public ContactHeaderImpl clone() {
        return (ContactHeaderImpl)super.clone();
    }

    @Override
    public ContactHeaderImpl ensure() {
        return this;
    }

}
