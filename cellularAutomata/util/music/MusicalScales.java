/*
 MusicalScales -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2005  David B. Bahr (http://academic.regis.edu/dbahr/)

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package cellularAutomata.util.music;

/**
 * Contains methods that convert the number of a note on a scale into a piano's
 * key number. For example, in the C major scale, the scale number 0 would be a
 * C, the scale number 1 would be a D, the scale number 2 would be an E, the
 * scale number 3 would be an F, etc. The piano's keys are numbered from 0 to 87
 * with 39 as middle C. So in the C major scale, the scale number 0 maps to a C
 * which is the piano key number 39. And in the C major scale, the scale number
 * 1 maps to a D which is the piano key number 41. (Note that the C major scale
 * skips the C# which is key number 40).
 * <p>
 * In the C blues scale, the scale number 0 is a C, which is key number 39 on
 * the piano. But in the C blues scale the scale number 1 is an Eb, which is
 * piano key number 42.
 * 
 * @author David Bahr
 */
public class MusicalScales
{
    /**
     * Value corresponding to an approximate soprano range.
     */
    public static final int SOPRANO = +36;

    /**
     * Value corresponding to an approximate mezzo-soprano range.
     */
    public static final int MEZZO_SOPRANO = +24;
    
    /**
     * Value corresponding to an approximate alto range.
     */
    public static final int ALTO = +12;
    
    /**
     * Value corresponding to an approximate tenor range.
     */
    public static final int TENOR = 0;
    
    /**
     * Value corresponding to an approximate baritone range.
     */
    public static final int BARITONE = -12;
    
    /**
     * Value corresponding to an approximate bass range.
     */
    public static final int BASSO = -24;
    
    /**
     * Value corresponding to an approximate basso profundo range.
     */
    public static final int BASSO_PROFUNDO = -36;
    
    
    // length of the blues scale
    private static final int BLUES_SCALE_LENGTH_ONE_OCTAVE = 7;

    // length of the major scale
    private static final int MAJOR_SCALE_LENGTH_ONE_OCTAVE = 8;

    // length of the harmonic minor scale
    private static final int MINOR_SCALE_LENGTH_ONE_OCTAVE = 8;

    // length of the pentatonic scale
    private static final int PENTATONIC_SCALE_LENGTH_ONE_OCTAVE = 6;

    // Unnecessary. All methods are static.
    private MusicalScales()
    {
    }

    /**
     * For any given beat constructs an appropriate chord for the C blues.
     * 
     * @param currentBeat
     *            The current beat.
     * 
     * @param octave
     *            The octave of notes that will be returned. Possible values
     *            include TENOR, BASSO, ALTO, etc.
     * 
     * @return An array of the pian key numbers that correspond to a chord.
     */
    public static int[] getCBluesChord(long currentBeat, int octave)
    {
        // the array of notes that will be returned
        int[] chord = null;

        // just in case
        if(currentBeat < 1)
        {
            currentBeat = 1;
        }

        // there are 12 measures times 4 beats/measure, or
        // 48 beats per refrain in the blues
        int beat = (int) (currentBeat % 48);

        // play a chord on the first beat of each measure
        if(beat % 4 == 0)
        {
            // C, F7, C, C7, F7, F7, C, C, G7, F7, C, C
            switch(beat)
            {
                // C chord
                case 0:
                    chord = new int[3];
                    chord[0] = 39;
                    chord[1] = 43;
                    chord[2] = 46;
                    break;
                // F7 chord
                case 4:
                    chord = new int[4];
                    chord[0] = 44;
                    chord[1] = 48;
                    chord[2] = 51;
                    chord[3] = 54;
                    break;
                // C chord
                case 8:
                    chord = new int[3];
                    chord[0] = 39;
                    chord[1] = 43;
                    chord[2] = 46;
                    break;
                // C7 chord
                // case 12:
                // chord = new int[4];
                // chord[0] = 39;
                // chord[1] = 43;
                // chord[2] = 46;
                // chord[3] = 49;
                // F7 chord
                case 16:
                    chord = new int[4];
                    chord[0] = 44;
                    chord[1] = 48;
                    chord[2] = 51;
                    chord[3] = 54;
                    break;
                // F7 chord
                // case 20:
                // chord = new int[4];
                // chord[0] = 44;
                // chord[1] = 48;
                // chord[2] = 51;
                // chord[3] = 54;
                // break;
                // C chord
                case 24:
                    chord = new int[3];
                    chord[0] = 39;
                    chord[1] = 43;
                    chord[2] = 46;
                    break;
                // C chord
                // case 28:
                // chord = new int[3];
                // chord[0] = 39;
                // chord[1] = 43;
                // chord[2] = 46;
                // break;
                // G7 chord
                case 32:
                    chord = new int[4];
                    chord[0] = 46;
                    chord[1] = 50;
                    chord[2] = 53;
                    chord[3] = 56;
                    break;
                // F7 chord
                case 36:
                    chord = new int[4];
                    chord[0] = 44;
                    chord[1] = 48;
                    chord[2] = 51;
                    chord[3] = 54;
                    break;
                // C chord
                case 40:
                    chord = new int[3];
                    chord[0] = 39;
                    chord[1] = 43;
                    chord[2] = 46;
                    break;
            // C chord
            // case 44:
            // chord = new int[3];
            // chord[0] = 39;
            // chord[1] = 43;
            // chord[2] = 46;
            // break;
            }
        }

        // change the octave
        if(chord != null)
        {
            for(int i = 0; i < chord.length; i++)
            {
                chord[i] += octave;
            }
        }

        return chord;
    }

    /**
     * For the given number of the note in the C blues scale, this returns the
     * piano key. For the TENOR octave, this scale starts at key number 39
     * (middle C), and ends at key number 50 (C an octave higher). See class
     * description for more details.
     * 
     * @param scaleNumber
     *            The number of the note on the scale. Values outside 0 to 6 are
     *            reassigned to the absolute value of modulo 7 (so for example,
     *            a -1 corresponds to a 1, and a 9 corresponds to a 3).
     * 
     * @param octave
     *            The octave of notes that will be returned. Possible values
     *            include TENOR, BASSO, ALTO, etc.
     * 
     * @return The piano's corresponding key number.
     */
    public static int getCBluesScaleOneOctave(int scaleNumber, int octave)
    {
        // the note that will be returned
        int note = 39;

        // the length of the scale
        int scaleLength = BLUES_SCALE_LENGTH_ONE_OCTAVE;

        // make sure the number is within range
        scaleNumber = Math.abs(scaleNumber % scaleLength);

        // the conversion to a C major scale starting at middle C
        switch(scaleNumber)
        {
            // middle C
            case 0:
                note = 39;
                break;
            // Eb
            case 1:
                note = 42;
                break;
            // F
            case 2:
                note = 44;
                break;
            // Gb
            case 3:
                note = 45;
                break;
            // G
            case 4:
                note = 46;
                break;
            // Bb
            case 5:
                note = 49;
                break;
            // C
            case 6:
                note = 51;
                break;
        }
        
        // change the octave
        note += octave;
       
        return note;
    }

    /**
     * The length of the blues scale (for one octave).
     * 
     * @return The length of the one-octave blues scale.
     */
    public static int getCBluesScaleOneOctaveLength()
    {
        return BLUES_SCALE_LENGTH_ONE_OCTAVE;
    }

    /**
     * For the given number of the note in the C major scale, this returns the
     * piano key. This scale starts at key number 39 (middle C), and ends at key
     * number 50 (C an octave higher). See class description for more details.
     * 
     * @param scaleNumber
     *            The number of the note on the scale. Values outside 0 to 7 are
     *            reassigned to the absolute value of modulo 8 (so for example,
     *            a -1 corresponds to a 1, and a 9 corresponds to a 2).
     * @param octave
     *            The octave of notes that will be returned. Possible values
     *            include TENOR, BASSO, ALTO, etc.
     * 
     * @return The piano's corresponding key number.
     */
    public static int getCMajorScaleOneOctave(int scaleNumber, int octave)
    {
        // the note that will be returned
        int note = 39;

        // the length of the scale
        int scaleLength = MAJOR_SCALE_LENGTH_ONE_OCTAVE;

        // make sure the number is within range
        scaleNumber = Math.abs(scaleNumber % scaleLength);

        // the conversion to a C major scale starting at middle C
        switch(scaleNumber)
        {
            // middle C
            case 0:
                note = 39;
                break;
            // D
            case 1:
                note = 41;
                break;
            // E
            case 2:
                note = 43;
                break;
            // F
            case 3:
                note = 44;
                break;
            // G
            case 4:
                note = 46;
                break;
            // A
            case 5:
                note = 48;
                break;
            // B
            case 6:
                note = 50;
                break;
            // C
            case 7:
                note = 51;
                break;
        }
        
        // change the octave
        note += octave;
        
        return note;
    }

    /**
     * The length of the C major scale (for one octave).
     * 
     * @return The length of the one-octave c-major scale.
     */
    public static int getCMajorScaleOneOctaveLength()
    {
        return MAJOR_SCALE_LENGTH_ONE_OCTAVE;
    }

    /**
     * For the given number of the note in the C harmonic minor scale, this
     * returns the piano key. This scale starts at key number 39 (middle C), and
     * ends at key number 50 (C an octave higher). See class description for
     * more details.
     * 
     * @param scaleNumber
     *            The number of the note on the scale. Values outside 0 to 7 are
     *            reassigned to the absolute value of modulo 8 (so for example,
     *            a -1 corresponds to a 1, and a 9 corresponds to a 2).
     * @param octave
     *            The octave of notes that will be returned. Possible values
     *            include TENOR, BASSO, ALTO, etc.
     * 
     * @return The piano's corresponding key number.
     */
    public static int getCHarmonicMinorScaleOneOctave(int scaleNumber, int octave)
    {
        // the note that will be returned
        int note = 39;

        // the length of the scale
        int scaleLength = MINOR_SCALE_LENGTH_ONE_OCTAVE;

        // make sure the number is within range
        scaleNumber = Math.abs(scaleNumber % scaleLength);

        // the conversion to a C major scale starting at middle C
        switch(scaleNumber)
        {
            // middle C
            case 0:
                note = 39;
                break;
            // D
            case 1:
                note = 41;
                break;
            // Eb
            case 2:
                note = 42;
                break;
            // F
            case 3:
                note = 44;
                break;
            // G
            case 4:
                note = 46;
                break;
            // Ab
            case 5:
                note = 47;
                break;
            // B
            case 6:
                note = 50;
                break;
            // C
            case 7:
                note = 51;
                break;
        }
        
        // change the octave
        note += octave;
        
        return note;
    }

    /**
     * The length of the C harmonic minor scale (for one octave).
     * 
     * @return The length of the one-octave c-harmonic-minor scale.
     */
    public static int getCHarmonicMinorScaleOneOctaveLength()
    {
        return MINOR_SCALE_LENGTH_ONE_OCTAVE;
    }

    /**
     * For the given number of the note in the C pentatonic scale, this returns
     * the piano key. This scale starts at key number 39 (middle C), and ends at
     * key number 50 (C an octave higher). See class description for more
     * details.
     * 
     * @param scaleNumber
     *            The number of the note on the scale. Values outside 0 to 5 are
     *            reassigned to the absolute value of modulo 6 (so for example,
     *            a -1 corresponds to a 1, and an 8 corresponds to a 2).
     * @param octave
     *            The octave of notes that will be returned. Possible values
     *            include TENOR, BASSO, ALTO, etc.
     * 
     * @return The piano's corresponding key number.
     */
    public static int getCPentatonicScaleOneOctave(int scaleNumber, int octave)
    {
        // the note that will be returned
        int note = 39;

        // the length of the scale
        int scaleLength = MINOR_SCALE_LENGTH_ONE_OCTAVE;

        // make sure the number is within range
        scaleNumber = Math.abs(scaleNumber % scaleLength);

        // the conversion to a C major scale starting at middle C
        switch(scaleNumber)
        {
            // middle C
            case 0:
                note = 39;
                break;
            // D
            case 1:
                note = 41;
                break;
            // F
            case 2:
                note = 44;
                break;
            // G
            case 3:
                note = 46;
                break;
            // A
            case 4:
                note = 48;
                break;
            // C
            case 5:
                note = 51;
                break;
        }
        
        // change the octave
        note += octave;
        
        return note;
    }

    /**
     * The length of the C pentatonic scale (for one octave).
     * 
     * @return The length of the one-octave c-pentatonic scale.
     */
    public static int getCPentatonicScaleOneOctaveLength()
    {
        return PENTATONIC_SCALE_LENGTH_ONE_OCTAVE;
    }

    /**
     * UNFINISHED! For the given number of the note in the scale, this returns
     * the piano key. This scale starts at the specified note, but the scale is
     * always in the key of C. So, for example, if the scale starts at D (but is
     * in the key of C), then this is really the D Dorian scale. See class
     * description for more details.
     * 
     * @param scaleNumber
     *            The number of the note on the scale. Values outside 0 to 7 are
     *            reassigned to the absolute value of modulo 8 (so for example,
     *            a -1 corresponds to a 1, and a 9 corresponds to a 2).
     * 
     * @param numberOfNotes
     *            The number of notes that are in the scale. For example, 15
     *            covers two octaves. Must be between 1 and 88 (the number of
     *            keys on a piano).
     * 
     * @param firstNote
     *            The first note of the scale. Must be between 0 and 87 (the
     *            number of keys on a piano). A 4039 is middle C, a 41 is middle
     *            D, etc.
     * 
     * @return The piano's corresponding key number.
     */
    public static int getCModalScale(int scaleNumber, int numberOfNotes,
        int firstNote)
    {
        // the note that will be returned
        int note = firstNote;

        // the length of the scale
        int scaleLength = numberOfNotes;

        // adjust the note to account for the starting position. For, example if
        // the first note is a D (41), then this adds 2.
        scaleNumber += (firstNote - 39);

        // make sure the number is within range
        scaleNumber = Math.abs(scaleNumber % scaleLength);

        // the conversion to a C major scale starting at the first note
        switch(scaleNumber)
        {
            // middle C
            case 0:
                note = 39;
                break;
            // D
            case 1:
                note = 41;
                break;
            // E
            case 2:
                note = 43;
                break;
            // F
            case 3:
                note = 44;
                break;
            // G
            case 4:
                note = 46;
                break;
            // A
            case 5:
                note = 48;
                break;
            // B
            case 6:
                note = 50;
                break;
            // C
            case 7:
                note = 51;
                break;
        }

        return note;
    }
}
