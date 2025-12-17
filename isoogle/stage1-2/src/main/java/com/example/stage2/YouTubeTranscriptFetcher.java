package com.example.stage2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * YouTubeTranscriptFetcher
 * ----------------------------------------------------
 * Responsible for fetching YouTube video transcripts (subtitles/captions).
 * 
 * This class extracts the video ID from YouTube URLs and retrieves
 * the transcript text using a Python helper script.
 * 
 * Features:
 *   - Extracts video ID from various YouTube URL formats
 *   - Calls Python script to fetch transcript
 *   - Handles errors gracefully
 *   - Uses Python's youtube-transcript-api library for reliable transcript fetching
 */
public class YouTubeTranscriptFetcher {

    private static final String PYTHON_SCRIPT = "fetch_youtube_transcript.py";
    private static final String VENV_PYTHON = ".venv/bin/python3";
    private static final String SYSTEM_PYTHON = "python3";

    /**
     * Fetches the transcript text from a YouTube video URL.
     * 
     * @param youtubeUrl The YouTube video URL (e.g., https://youtu.be/xxxxx or https://www.youtube.com/watch?v=xxxxx)
     * @return Transcript text as a single String, or empty String if failed
     */
    public static String fetchTranscript(String youtubeUrl) {
        try {
            // Extract video ID from URL
            String videoId = extractVideoId(youtubeUrl);
            if (videoId == null || videoId.isEmpty()) {
                System.out.println("[ERROR] Unable to extract video ID from: " + youtubeUrl);
                return "";
            }

            System.out.println("[INFO] Extracted Video ID: " + videoId);

            // Call Python script to fetch transcript
            String transcript = callPythonScript(videoId);
            
            if (transcript.isEmpty()) {
                System.out.println("[ERROR] Failed to fetch transcript for video: " + videoId);
                return "";
            }
            
            System.out.println("[SUCCESS] Transcript fetched successfully! Length: " + transcript.length() + " characters");
            return transcript;

        } catch (Exception e) {
            System.out.println("[ERROR] Failed to fetch YouTube transcript: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Extracts the video ID from various YouTube URL formats.
     * 
     * Supported formats:
     *   - https://www.youtube.com/watch?v=VIDEO_ID
     *   - https://youtu.be/VIDEO_ID
     *   - https://www.youtube.com/embed/VIDEO_ID
     *   - https://www.youtube.com/v/VIDEO_ID
     * 
     * @param url The YouTube URL
     * @return Video ID, or null if not found
     */
    private static String extractVideoId(String url) {
        // Pattern 1: youtu.be/VIDEO_ID
        Pattern pattern1 = Pattern.compile("youtu\\.be/([a-zA-Z0-9_-]+)");
        Matcher matcher1 = pattern1.matcher(url);
        if (matcher1.find()) {
            return matcher1.group(1);
        }

        // Pattern 2: youtube.com/watch?v=VIDEO_ID
        Pattern pattern2 = Pattern.compile("[?&]v=([a-zA-Z0-9_-]+)");
        Matcher matcher2 = pattern2.matcher(url);
        if (matcher2.find()) {
            return matcher2.group(1);
        }

        // Pattern 3: youtube.com/embed/VIDEO_ID or youtube.com/v/VIDEO_ID
        Pattern pattern3 = Pattern.compile("(?:embed|v)/([a-zA-Z0-9_-]+)");
        Matcher matcher3 = pattern3.matcher(url);
        if (matcher3.find()) {
            return matcher3.group(1);
        }

        return null;
    }

    /**
     * Calls the Python helper script to fetch the transcript.
     * 
     * @param videoId The YouTube video ID
     * @return Transcript text
     */
    private static String callPythonScript(String videoId) {
        BufferedReader reader = null;
        BufferedReader errorReader = null;
        
        try {
            // Determine which Python interpreter to use (prefer venv)
            String pythonCmd = getPythonCommand();
            
            // Build the command to call Python script
            String[] command = {pythonCmd, PYTHON_SCRIPT, videoId};
            
            System.out.println("[INFO] Calling Python script: " + pythonCmd + " " + PYTHON_SCRIPT + " " + videoId);
            
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(false);
            
            Process process = processBuilder.start();
            
            // Read stdout (transcript)
            reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), "UTF-8")
            );
            
            StringBuilder transcript = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                transcript.append(line).append(" ");
            }
            
            // Read stderr (errors and info messages)
            errorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream(), "UTF-8")
            );
            
            StringBuilder errors = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errors.append(line).append("\n");
            }
            
            // Wait for process to complete
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                System.out.println("[ERROR] Python script failed with exit code: " + exitCode);
                if (errors.length() > 0) {
                    System.out.println("[ERROR] Python error output:");
                    System.out.println(errors.toString());
                }
                return "";
            }
            
            // Print any error messages (could be warnings)
            if (errors.length() > 0) {
                System.out.println("[INFO] Python script output:");
                System.out.println(errors.toString().trim());
            }
            
            return transcript.toString().trim();
            
        } catch (Exception e) {
            System.out.println("[ERROR] Error calling Python script: " + e.getMessage());
            e.printStackTrace();
            return "";
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignore) {}
            }
            if (errorReader != null) {
                try {
                    errorReader.close();
                } catch (Exception ignore) {}
            }
        }
    }

    /**
     * Gets the Python command to use (prefers virtual environment).
     * 
     * @return Path to Python interpreter
     */
    private static String getPythonCommand() {
        // Check if virtual environment exists
        java.io.File venvPython = new java.io.File(VENV_PYTHON);
        if (venvPython.exists() && venvPython.canExecute()) {
            System.out.println("[INFO] Using Python from virtual environment: " + VENV_PYTHON);
            return VENV_PYTHON;
        }
        
        // Fall back to system Python
        System.out.println("[INFO] Virtual environment not found, using system Python: " + SYSTEM_PYTHON);
        System.out.println("[WARN] Please run: ./setup_python.sh to set up the virtual environment");
        return SYSTEM_PYTHON;
    }
}
