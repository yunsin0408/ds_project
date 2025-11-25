#!/usr/bin/env python3
"""
YouTube Transcript Fetcher - Python Helper Script
--------------------------------------------------
This script fetches YouTube video transcripts using the youtube-transcript-api library.
It's designed to be called from Java code.

Usage:
    python fetch_youtube_transcript.py <video_id_or_url>

Output:
    Prints the transcript text to stdout
"""

import sys
import re
from typing import Optional


def extract_video_id(url: str) -> Optional[str]:
    """
    Extract video ID from various YouTube URL formats.
    
    Args:
        url: YouTube URL or video ID
        
    Returns:
        Video ID if found, None otherwise
    """
    # If it's already just an ID (11 characters, alphanumeric with - and _)
    if re.match(r'^[a-zA-Z0-9_-]{11}$', url):
        return url
    
    # Pattern 1: youtu.be/VIDEO_ID
    match = re.search(r'youtu\.be/([a-zA-Z0-9_-]+)', url)
    if match:
        return match.group(1)
    
    # Pattern 2: youtube.com/watch?v=VIDEO_ID
    match = re.search(r'[?&]v=([a-zA-Z0-9_-]+)', url)
    if match:
        return match.group(1)
    
    # Pattern 3: youtube.com/embed/VIDEO_ID or youtube.com/v/VIDEO_ID
    match = re.search(r'(?:embed|v)/([a-zA-Z0-9_-]+)', url)
    if match:
        return match.group(1)
    
    return None


def fetch_transcript(video_id: str) -> str:
    """
    Fetch transcript for a YouTube video.
    
    Args:
        video_id: YouTube video ID
        
    Returns:
        Transcript text as a single string
    """
    try:
        from youtube_transcript_api import YouTubeTranscriptApi
        
        # Create API instance
        api = YouTubeTranscriptApi()
        
        # Try to get transcript (prefer English, but accept any available language)
        try:
            # Try English first using the shortcut method
            fetched_transcript = api.fetch(
                video_id, 
                languages=['en', 'en-US', 'en-GB']
            )
        except:
            # If English not available, try to get any available transcript
            try:
                # Get list of available transcripts
                transcript_list = api.list(video_id)
                # Get the first available transcript
                transcript = next(iter(transcript_list))
                fetched_transcript = transcript.fetch()
            except:
                # Last resort: just try to get the default transcript
                fetched_transcript = api.fetch(video_id)
        
        # Combine all text segments (snippets is the list of transcript entries)
        # Each snippet has attributes: text, start, duration
        full_text = ' '.join([snippet.text for snippet in fetched_transcript.snippets])
        
        return full_text
        
    except ImportError:
        print("ERROR: youtube-transcript-api library not installed", file=sys.stderr)
        print("Install it with: pip install youtube-transcript-api", file=sys.stderr)
        return ""
    except Exception as e:
        print(f"ERROR: {str(e)}", file=sys.stderr)
        return ""


def main():
    """Main function."""
    if len(sys.argv) < 2:
        print("Usage: python fetch_youtube_transcript.py <video_id_or_url>", file=sys.stderr)
        sys.exit(1)
    
    url_or_id = sys.argv[1]
    
    # Extract video ID
    video_id = extract_video_id(url_or_id)
    
    if not video_id:
        print(f"ERROR: Could not extract video ID from: {url_or_id}", file=sys.stderr)
        sys.exit(1)
    
    # Fetch transcript
    transcript = fetch_transcript(video_id)
    
    if transcript:
        # Print transcript to stdout (Java will read this)
        print(transcript)
    else:
        sys.exit(1)


if __name__ == "__main__":
    main()

