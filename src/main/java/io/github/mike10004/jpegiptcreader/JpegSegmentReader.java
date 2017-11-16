/*
 * Copyright 2002-2017 Drew Noakes
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * More information about this project is available at:
 *
 *    https://drewnoakes.com/code/exif/
 *    https://github.com/drewnoakes/metadata-extractor
 */
package io.github.mike10004.jpegiptcreader;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.imaging.jpeg.JpegSegmentMetadataReader;
import com.drew.imaging.jpeg.JpegSegmentType;
import com.drew.lang.SequentialReader;
import com.drew.lang.StreamReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Obtains all available metadata from JPEG formatted files.
 *
 * @author Drew Noakes https://drewnoakes.com
 */
public class JpegSegmentReader
{
    /**
     * The 0xFF byte that signals the start of a segment.
     */
    private static final byte SEGMENT_IDENTIFIER = (byte) 0xFF;

    /**
     * Private, because this segment crashes my algorithm, and searching for it doesn't work (yet).
     */
    private static final byte SEGMENT_SOS = (byte) 0xDA;

    /**
     * Private, because one wouldn't search for it.
     */
    private static final byte MARKER_EOI = (byte) 0xD9;


    public JpegSegmentSpecSet readMetadata(InputStream inputStream, JpegSegmentMetadataReader reader) throws JpegProcessingException, IOException {

        Set<JpegSegmentType> segmentTypes = new HashSet<>();
        reader.getSegmentTypes().forEach(segmentTypes::add);
        JpegSegmentSpecSet segmentData = readSegments(new StreamReader(inputStream), segmentTypes);
        return segmentData;
    }

    private static JpegSegmentSpecSet readSegments(final SequentialReader reader, Iterable<JpegSegmentType> segmentTypes) throws JpegProcessingException, IOException     {
        Objects.requireNonNull(segmentTypes);
        // Must be big-endian
        assert (reader.isMotorolaByteOrder());

        // first two bytes should be JPEG magic number
        final int magicNumber = reader.getUInt16();
        if (magicNumber != 0xFFD8) {
            throw new JpegProcessingException("JPEG data is expected to begin with 0xFFD8 (ÿØ) not 0x" + Integer.toHexString(magicNumber));
        }

        Set<Byte> segmentTypeBytes = new HashSet<>();
        for (JpegSegmentType segmentType : segmentTypes) {
            segmentTypeBytes.add(segmentType.byteValue);
        }

        JpegSegmentSpecSet segmentData = JpegSegmentSpecSet.create();

        do {
            long segmentStart = reader.getPosition();
            // Find the segment marker. Markers are zero or more 0xFF bytes, followed
            // by a 0xFF and then a byte not equal to 0x00 or 0xFF.

            byte segmentIdentifier = reader.getInt8();
            byte segmentType = reader.getInt8();

            // Read until we have a 0xFF byte followed by a byte that is not 0xFF or 0x00
            while (segmentIdentifier != SEGMENT_IDENTIFIER || segmentType == SEGMENT_IDENTIFIER || segmentType == 0) {
                segmentIdentifier = segmentType;
                segmentType = reader.getInt8();
            }

            if (segmentType == SEGMENT_SOS) {
                // The 'Start-Of-Scan' segment's length doesn't include the image data, instead would
                // have to search for the two bytes: 0xFF 0xD9 (EOI).
                // It comes last so simply return at this point
                return segmentData;
            }

            if (segmentType == MARKER_EOI) {
                // the 'End-Of-Image' segment -- this should never be found in this fashion
                return segmentData;
            }

            // next 2-bytes are <segment-size>: [high-byte] [low-byte]
            int segmentLength = reader.getUInt16();

            // segment length includes size bytes, so subtract two
            segmentLength -= 2;

            if (segmentLength < 0)
                throw new JpegProcessingException("JPEG segment size would be less than zero");

            // Check whether we are interested in this segment
            if (segmentTypeBytes.contains(segmentType)) {
                long segmentContentStart = reader.getPosition();
                reader.skip(segmentLength);
                // skip throws EOF if it can't skip as much as specified
                segmentData.addSegment(JpegSegmentType.fromByte(segmentType), segmentStart, segmentContentStart, segmentLength);
            } else {
                // Some if the JPEG is truncated, just return what data we've already gathered
                if (!reader.trySkip(segmentLength)) {
                    return segmentData;
                }
            }

        } while (true);
    }

}
