/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.header.ContactHeader;
import io.pkts.packet.sip.header.MultiAddressParametersHeader.AddressEntry;

import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 *
 */
public class ContactHeaderTest {

    @Test
    public void testBriaContactHeader() {
        final Buffer buffer =
                Buffers.wrap("<sip:hello@10.0.1.5:51945;ob>;reg-id=1;+sip.instance=\"<urn:uuid:D5E3DFFEFC3E4B69BCDFCC5DAC7BDEA9326B2EB8>\"");
        final ContactHeader contact = ContactHeader.frame(buffer);
		final AddressEntry entry = contact.getEntry(0);
		final SipURI uri = contact.getEntry(0).uri;
//        final Address address = contact.getAddress();
//        assertThat(address.getDisplayName(), is(Buffers.EMPTY_BUFFER));
//        final SipURI uri = (SipURI) address.getURI();
        assertThat(uri.getUser().toString(), is("hello"));
        assertThat(uri.getHost().toString(), is("10.0.1.5"));
        assertThat(uri.getPort(), is(51945));
        assertThat(uri.getParameter("ob"), is(Buffers.EMPTY_BUFFER));


        // note, asking for a parameter on the actual header is NOT the same
        // as doing it on the actual URI. Now you are asking for a header parameter
        // instead... in this case, it doesn't exist anyway but whatever...
//        assertThat(contact.getParameter("expires"), is((Buffer) null));
//        assertThat(contact.getParameter("+sip.instance").toString(),
        assertThat(entry.headerParams.getParameter("expires"), is((Buffer) null));
        assertThat(entry.headerParams.getParameter("+sip.instance").toString(),
                is("<urn:uuid:D5E3DFFEFC3E4B69BCDFCC5DAC7BDEA9326B2EB8>"));

    }

    @Test
    public void testBuildContact() throws Exception {
        final SipURI uri = SipURI.frame(Buffers.wrap("sip:hello@10.0.1.5:51945;ob"));
        System.out.println(uri);
        uri.setParameter("expires", 600);
        System.out.println(uri);
        final ContactHeader contact = ContactHeader.with().address(uri).build();
        contact.toString();
        assertThat(contact.toString(), is("Contact: <sip:hello@10.0.1.5:51945;ob;expires=600>"));
    }

}
