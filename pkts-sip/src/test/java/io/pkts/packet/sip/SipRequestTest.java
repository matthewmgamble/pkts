/**
 * 
 */
package io.pkts.packet.sip;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import io.pkts.PktsTestBase;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.CallIdHeader;
import io.pkts.packet.sip.header.ContactHeader;
import io.pkts.packet.sip.header.FromHeader;
import io.pkts.packet.sip.header.MaxForwardsHeader;
import io.pkts.packet.sip.header.ViaHeader;
import io.pkts.packet.sip.impl.SipParser;

import org.junit.Before;
import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipRequestTest extends PktsTestBase {

    private FromHeader from;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.from = FromHeader.with().user("bob").host("somewhere.com").build();
    }


    /**
     * Test to create a new INVITE request and check all the headers that are supposed to be created
     * by default when not specified indeed are created with the correct values.
     * 
     * @throws Exception
     */
    @Test
    public void testCreateInvite() throws Exception {
        final SipRequest invite = SipRequest.invite("sip:alice@example.com").from(this.from).build();
        assertThat(invite.getToHeader().toString(), is("To: <sip:alice@example.com>"));

        final CSeqHeader cseq = invite.getCSeqHeader();
        assertThat(cseq.getSeqNumber(), is(0L));
        assertThat(cseq.getMethod().toString(), is("INVITE"));

        final CallIdHeader callId = invite.getCallIDHeader();
        assertThat(callId, not((CallIdHeader) null));

        final MaxForwardsHeader max = invite.getMaxForwards();
        assertThat(max.getMaxForwards(), is(70));

        assertThat(invite.getFromHeader().toString(), is("From: <sip:bob@somewhere.com>"));
    }

    /**
     * Although not mandatory from the builder's perspective, having a request without a
     * {@link ContactHeader} is pretty much useless so make sure that we can add that as well.
     * 
     * @throws Exception
     */
    @Test
    public void testCreateInviteWithContactHeader() throws Exception {
        //final ContactHeader contact = ContactHeader.with().host("12.13.14.15").port(1234).transportTCP().build();
		final ContactHeader contact = 
				ContactHeader
						.with()
						.address(SipURI
								.with()
								.host("12.13.14.15")
								.port(1234)
								.transport(SipParser.TCP)
								.build())
						.build();
        final SipRequest invite = SipRequest.invite("sip:alice@example.com").from(this.from).contact(contact).build();
        //final SipURI contactURI = (SipURI) invite.getContactHeader().getAddress().getURI();
		final SipURI contactURI = (SipURI) invite.getContactHeader().getEntry(0).uri;
        assertThat(contactURI.getPort(), is(1234));
        assertThat(contactURI.getHost().toString(), is("12.13.14.15"));
        assertThat(contact.getValue().toString(), is("<sip:12.13.14.15:1234;transport=tcp>"));
    }

    @Test
    public void testCreateInviteWithViaHeaders() throws Exception {
        final ViaHeader via =
                ViaHeader.with().host("127.0.0.1").port(9898).transportUDP().branch(ViaHeader.generateBranch()).build();
        SipRequest invite = SipRequest.invite("sip:alice@example.com").from(this.from).via(via).build();

        // since there is only one via header, getting the "top-most" via header should
        // be the same as getting the first via off of the list.
        assertThat(invite.getViaHeaders().size(), is(1));
        assertThat(
                invite.getViaHeaders().get(0).toString()
                .startsWith("Via: SIP/2.0/UDP 127.0.0.1:9898;branch=z9hG4bK"), is(true));
        assertThat(invite.getViaHeader().toString().startsWith("Via: SIP/2.0/UDP 127.0.0.1:9898;branch=z9hG4bK"),
                is(true));

        // two via headers
        final ViaHeader via2 =
                ViaHeader.with().host("192.168.0.100").transportTCP().branch(ViaHeader.generateBranch()).build();
        invite = SipRequest.invite("sip:alice@example.com").from(this.from).via(via).via(via2).build();
        assertThat(invite.getViaHeaders().size(), is(2));

        // the top-most via should be the one we added first.
        assertThat(invite.getViaHeader().toString().startsWith("Via: SIP/2.0/UDP 127.0.0.1:9898;branch=z9hG4bK"),
                is(true));

        assertThat(
                invite.getViaHeaders().get(1).toString().startsWith("Via: SIP/2.0/TCP 192.168.0.100;branch=z9hG4bK"),
                is(true));
    }

}
