/*
 MusicalInstrument -- a class within the Cellular Automaton Explorer. 
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

import javax.sound.midi.*;
import javax.sound.midi.MidiUnavailableException;

/**
 * Plays musical notes that correspond to the notes on a piano. A piano has keys
 * numbered from 1 (lowest "A") to 88 ("highest "C"). By selecting a number from
 * 1 to 88, the play method will reproduce the corresponding piano's note.
 * Values larger than 88 and less than 1 will throw an
 * <code>IllegalArgumentException</code>.
 * <p>
 * Silent notes or "rests" can be played by specifying any key with a volume of
 * 0 and a nonzero duration.
 * <p>
 * The duration of each note can be specified in milliseconds. The volume is a
 * number between 0 and 127 with 0 being inaudible and 127 being the loudest.
 * Default values for duration and volume are 150 milliseconds and 60 unless
 * reset after the class is instantiated.
 * <p>
 * A synthesizer or midi is used to reproduce the notes. The synthesizer must be
 * opened prior to use (<code>openSynthesizer</code>) and closed after use (<code>closeSynthesizer</code>).
 * <p>
 * An example of use:
 * 
 * <pre>
 * MusicalInstrument piano = new MusicalInstrument();
 * 
 * try
 * {
 *     piano.openSynthesizer();
 * 
 *     //play chromatic scale
 *     for(int i = 0; i &lt;= 88; i++)
 *     {
 *         piano.play(i);
 *     }
 * }
 * catch(javax.sound.midi.MidiUnavailableException e)
 * {
 *     System.out.println(&quot;Could not open midi. &quot; + e.toString());
 * }
 * finally
 * {
 *     //a finally clause is always executed at the end of a
 *     //try-catch clause, even if a failure made the code 
 *     //enter the catch clause.  So this is a very safe way 
 *     //to close the synthesizer.  Will always happen!
 *     piano.closeSynthesizer();
 * }
 * </pre>
 * 
 * This code was authored by David Bahr and is only intended for use with Regis
 * University computer science classes.
 * 
 * @author David Bahr
 * @version 1.3 11/1/05
 */
public class MusicalInstrument
{
    // -------------------------------------------------
    // constants

    /**
     * Message generated when there is an error because no instruments are
     * available for the synthesizer.
     */
    public final static String NO_INSTRUMENTS_MESSAGE = "No instruments are "
        + "available.";

    /**
     * Message generated when there is an error because no sound bank is
     * available for the synthesizer.
     */
    public final static String NO_SOUND_BANK_MESSAGE = "No soundbank is "
        + "available.";

    /**
     * Message generated when there is an error because there is no synthesizer
     * available.
     */
    public final static String NO_SYNTHESIZER_MESSAGE = "No synthesizer is "
        + "available.";

    // The number of notes on a piano.
    private final static int NOTENUM = 88;

    // -------------------------------------------------

    // keeps track of which notes are currently playing (on). There are 0-127
    // available midi notes.
    private boolean[] notesOn = new boolean[128];

    // Default duration in milliseconds
    private int defaultDuration = 150;

    // Default volume (number between 1 and 127)
    private int defaultVolume = 60;

    // The default midi channel
    private MidiChannel[] channels = null;

    // The synthesizer used to play a note
    private Synthesizer synth = null;

    // -------------------------------------------------
    // constructors

    /**
     * Create a piano synthesizer.
     */
    public MusicalInstrument()
    {
        // indicate that no notes are currently playing
        for(int i = 0; i < notesOn.length; i++)
        {
            notesOn[i] = false;
        }

    }

    // -------------------------------------------------
    // methods

    /**
     * Maps the piano key's number to a value recognized by the midi. Piano
     * notes range from 0 to 87 with 39 being "middle C", but the midi can
     * handle notes from 0 to 127 with 60 being "middle C".
     */
    private int mapPianoNoteToMidiNote(int note)
        throws IllegalArgumentException
    {
        if(note < 0 || note > NOTENUM - 1)
        {
            throw new IllegalArgumentException("Note must be between 0 and "
                + (NOTENUM - 1) + " (the notes on a piano).");
        }

        int midiNote = note + 21;

        // just in case
        if(midiNote < 0)
        {
            midiNote = 0;
        }

        // just in case
        if(midiNote > 127)
        {
            midiNote = 127;
        }

        return midiNote;
    }

    /**
     * Turns off all notes currently being played.
     * 
     * @exception MidiUnavailableException
     *                if Midi Synthesizer has not been opened.
     */
    public void allNotesOff()
    {
        if(synth.isOpen())
        {
            channels[0].allNotesOff();
        }

        // keep track that all notes are now off
        for(int i = 0; i < notesOn.length; i++)
        {
            notesOn[i] = false;
        }
    }

    /**
     * Closes the synthesizer properly so that it does not interfere with other
     * applications. If the synthesizer is not open, then this method will not
     * close the synthesizer.
     */
    public void closeSynthesizer()
    {
        if((synth != null) && synth.isOpen())
        {
            synth.close();
        }
        synth = null;
    }

    /**
     * An array of instruments loaded into the synthesizer;
     * 
     * @return An array of instruments.
     */
    public Instrument[] getLoadedInstruments()
    {
        return synth.getLoadedInstruments();
    }

    /**
     * The default duration in milliseconds. 150 milliseconds unless reset with
     * setDefaultDuration.
     * 
     * @return default duration of note
     */
    public int getDefaultDuration()
    {
        return defaultDuration;
    }

    /**
     * The default volume as a number between 0 (inaudible) and 127 (loudest).
     * Default is 60 unless reset with setDefaultVolume.
     * 
     * @return default volume of note
     */
    public int getDefaultVolume()
    {
        return defaultVolume;
    }

    /**
     * The number of keys that are on a piano (the number of tones that can be
     * played by this synthesizer).
     * 
     * @return the number of keys on a piano
     */
    public int getNumberOfPianoKeys()
    {
        return NOTENUM;
    }

    /**
     * Checks if the given piano key is already playing.
     * 
     * @param keyNumber
     *            The piano key (note) that will be checked to see if it is
     *            currently playing.
     * 
     * @return true if the specified note is playing
     */
    public boolean isNoteOn(int keyNumber)
    {
        int midiNote = mapPianoNoteToMidiNote(keyNumber);

        return notesOn[midiNote];
    }

    /**
     * Tests for presence of the sythensizer.
     * 
     * @return <code>true</code> if synthesizer is open
     */
    public boolean isOpen()
    {
        boolean open = false;

        if(synth != null)
        {
            open = synth.isOpen();
        }

        return open;
    }

    /**
     * Turns off the note corresponding to the key number on the piano (0 to
     * 87). A piano has 88 keys and this method starts counting from 0.
     * Therefore a 0 is the lowest "A" on the piano and a 39 is "middle C".
     * 
     * @param keyNumber
     *            A piano key's number between 0 and 87.
     * @exception MidiUnavailableException
     *                if Midi Synthesizer has not been opened.
     */
    public void noteOff(int keyNumber) throws MidiUnavailableException
    {
        int midiNote = mapPianoNoteToMidiNote(keyNumber);

        if(synth.isOpen())
        {
            channels[0].noteOff(midiNote);

            // keep track that this note is currently off
            notesOn[midiNote] = false;
        }
        else
        {
            throw new MidiUnavailableException(
                "You did not open the Midi Synthesizer.");
        }
    }

    /**
     * Plays the note corresponding to the key number on the piano (0 to 87)
     * with the given volume. The note's duration is infinite and will continue
     * sounding until it is turned off (see method noteOff()). A piano has 88
     * keys and this method starts counting from 0. Therefore a 0 is the lowest
     * "A" on the piano and a 39 is "middle C".
     * 
     * @param keyNumber
     *            A piano key's number between 0 and 87.
     * @exception MidiUnavailableException
     *                if Midi Synthesizer has not been opened.
     */
    public void noteOn(int keyNumber) throws MidiUnavailableException
    {
        noteOn(keyNumber, defaultVolume);
    }

    /**
     * Plays the note corresponding to the key number on the piano (0 to 87)
     * with the given volume. The note's duration is infinite and will continue
     * sounding until it is turned off (see method noteOff()). A piano has 88
     * keys and this method starts counting from 0. Therefore a 0 is the lowest
     * "A" on the piano and a 39 is "middle C".
     * 
     * @param keyNumber
     *            A piano key's number between 0 and 87.
     * @param volume
     *            Number between 0 and 127.
     * @exception MidiUnavailableException
     *                if Midi Synthesizer has not been opened.
     */
    public void noteOn(int keyNumber, int volume)
        throws MidiUnavailableException
    {
        int midiNote = mapPianoNoteToMidiNote(keyNumber);

        if(synth.isOpen())
        {
            channels[0].noteOn(midiNote, volume);

            // keep track that this note is currently on
            notesOn[midiNote] = true;
        }
        else
        {
            throw new MidiUnavailableException(
                "You did not open the Midi Synthesizer.");
        }
    }

    /**
     * Request the default synthesizer (used to play music).
     * 
     * @exception MidiUnavailableException
     *                if Midi Synthesizer is unavailable
     */
    public void openSynthesizer() throws MidiUnavailableException
    {
        if(synth == null)
        {
            synth = MidiSystem.getSynthesizer();

            if(synth != null)
            {
                synth.open();

                Soundbank soundbank = synth.getDefaultSoundbank();

                if(soundbank != null)
                {
                    synth.loadAllInstruments(soundbank);
                }
                else
                {
                    throw new MidiUnavailableException(NO_SOUND_BANK_MESSAGE);
                }

                channels = synth.getChannels();

                Instrument[] inst = synth.getAvailableInstruments();
                if(inst == null || (inst.length == 0))
                {
                    throw new MidiUnavailableException(NO_INSTRUMENTS_MESSAGE);
                }
            }
            else
            {
                throw new MidiUnavailableException(NO_SYNTHESIZER_MESSAGE);
            }
        }
    }

    /**
     * Plays the note corresponding to the key number on the piano (0 to 87). A
     * piano has 88 keys and this method starts counting from 0. Therefore a 0
     * is the lowest "A" on the piano and a 39 is "middle C". The default
     * duration and volume are used.
     * 
     * @param keyNumber
     *            A piano key's number between 0 and 87.
     * @exception MidiUnavailableException
     *                if Midi Synthesizer has not been opened.
     */
    public void play(int keyNumber) throws MidiUnavailableException
    {
        play(keyNumber, defaultDuration, defaultVolume);
    }

    /**
     * Plays the note corresponding to the key number on the piano (0 to 87)
     * with the given duration. A piano has 88 keys and this method starts
     * counting from 0. Therefore a 0 is the lowest "A" on the piano and a 39 is
     * "middle C". The default volume is used.
     * 
     * @param keyNumber
     *            A piano key's number between 0 and 87.
     * @param duration
     *            In milliseconds.
     * @exception MidiUnavailableException
     *                if Midi Synthesizer has not been opened.
     */
    public void play(int keyNumber, int duration)
        throws MidiUnavailableException
    {
        play(keyNumber, duration, defaultVolume);
    }

    /**
     * Plays the note corresponding to the key number on the piano (0 to 87)
     * with the given duration and volume. A piano has 88 keys and this method
     * starts counting from 0. Therefore a 0 is the lowest "A" on the piano and
     * a 39 is "middle C".
     * 
     * @param keyNumber
     *            A piano key's number between 0 and 87.
     * @param volume
     *            Number between 0 and 127.
     * @param duration
     *            In milliseconds.
     * @exception MidiUnavailableException
     *                if Midi Synthesizer has not been opened.
     */
    public void play(int keyNumber, int duration, int volume)
        throws MidiUnavailableException
    {
        int midiNote = mapPianoNoteToMidiNote(keyNumber);

        if(synth.isOpen())
        {
            channels[0].noteOn(midiNote, volume);

            // keep track that this note is currently on
            notesOn[midiNote] = true;

            try
            {
                Thread.sleep(duration);
            }
            catch(InterruptedException e)
            {
                // ignored -- not fatal
            }

            channels[0].noteOff(midiNote);

            // keep track that this note is currently off
            notesOn[midiNote] = false;
        }
        else
        {
            throw new MidiUnavailableException(
                "You did not open the Midi Synthesizer.");
        }
    }

    /**
     * Sets default duration in milliseconds.
     * 
     * @param duration
     *            In milliseconds.
     * @throws IllegalArgumentException
     *             if less than 0.
     */
    public void setDefaultDuration(int duration)
    {
        if(duration < 0)
        {
            throw new IllegalArgumentException(
                "Duration must be between greater than or " + "equal to 0.");
        }

        defaultDuration = duration;
    }

    /**
     * Sets the default volume as a number between 0 (inaudible) and 127
     * (loudest).
     * 
     * @param volume
     *            Between 0 and 127.
     * @throws IllegalArgumentException
     *             if not between 0 and 127.
     */
    public void setDefaultVolume(int volume) throws IllegalArgumentException
    {
        if(volume < 0 || volume > 127)
        {
            throw new IllegalArgumentException(
                "Volume must be between 0 and 127.");
        }

        defaultVolume = volume;
    }

    /**
     * Changes the instrument to the one specified, assuming that the instrument
     * is available.
     * 
     * @param instrumentName
     *            The instrument that will be played.
     */
    public void setInstrument(String instrumentName)
    {
        // find the instrument
        Instrument[] inst = synth.getLoadedInstruments();

        if((inst != null) && (inst.length > 0))
        {
            int i = 0;
            boolean notFound = true;
            while(notFound)
            {
                if(inst[i].getName().equals(instrumentName))
                {
                    // found that instrument
                    notFound = false;

                    // change the instrument
                    channels[0].programChange(inst[i].getPatch().getBank(),
                        inst[i].getPatch().getProgram());
                }

                // try the next instrument
                i++;
            }
        }
    }
}
