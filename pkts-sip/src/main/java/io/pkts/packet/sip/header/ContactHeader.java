package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.header.impl.ContactHeaderImpl;
import io.pkts.packet.sip.header.impl.ParametersSupport;
import io.pkts.packet.sip.impl.SipParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ContactHeader extends MultiAddressParametersHeader {

    Buffer NAME = Buffers.wrap("Contact");

    @Override
    ContactHeader clone();

    static Builder with() {
        return new Builder();
    }

    /**
     * Frame the value as a {@link ContactHeader}.
     * 
     * @param value
     * @return
     * @throws SipParseException in case anything goes wrong while parsing.
     */
    public static ContactHeader frame(final Buffer buffer) throws SipParseException {
		// Putting it all in here since lack of consistancy in the Library
		// Parse Quoted display name if exists
		// Parse < if exists
		// Parse URI
		// Parse URI parameters if < was found
		// Parse > if < was found
		// Parse parameters if ; is next
		// Repeat if , is next
		List<AddressEntry> address = new ArrayList();
		
		try {
		
			while(buffer.hasReadableBytes()){
				Buffer displayName = null;
				SipURI uri = null;
				Buffer headerParams = null;
				displayName = SipParser.consumeDisplayName(buffer);
				if(SipParser.isNext(buffer, SipParser.LAQUOT)){
					buffer.readByte();
					int endIndex = buffer.indexOf(1024, SipParser.RAQUOT);
					if(endIndex < 0)
						uri = SipURI.frame(buffer);
					else
						uri = SipURI.frame(buffer.readBytes(endIndex - buffer.getReaderIndex()));
					buffer.readByte();
				} else {
					int endIndex = buffer.indexOf(1024, SipParser.COMMA, SipParser.SEMI, SipParser.CR, SipParser.LF);
					if(endIndex < 0)
						uri = SipURI.frame(buffer);
					else
						uri = SipURI.frame(buffer.readBytes(endIndex - buffer.getReaderIndex()));
				}
				if(SipParser.isNext(buffer, SipParser.SEMI)){
					int endIndex = buffer.indexOf(1024, SipParser.COMMA, SipParser.CR, SipParser.LF);
					if(endIndex < 0)
						headerParams = buffer.readBytes(buffer.capacity() - buffer.getReaderIndex());
					else
						headerParams = buffer.readBytes(endIndex - buffer.getReaderIndex());
				}

				AddressEntry entry = new AddressEntry(uri);
				if(displayName != null)
					entry.displayName = displayName.toString();
				if(headerParams != null)
					entry.headerParams = new ParametersSupport(headerParams);
				address.add(entry);				
				
				if(buffer.hasReadableBytes() && SipParser.isNext(buffer, SipParser.COMMA))
					buffer.readByte();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return new ContactHeaderImpl(NAME, address);
    }

    static class Builder extends MultiAddressParametersHeader.Builder<ContactHeader> {

        private Builder() {
            super(NAME);
        }
		
        @Override
        public ContactHeader internalBuild(Buffer name, final List<AddressEntry> address) throws SipParseException {
            return new ContactHeaderImpl(name, address);
        }
    }

}
