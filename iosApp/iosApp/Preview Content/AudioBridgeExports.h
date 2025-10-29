#pragma once

#ifdef __cplusplus
extern "C" {
#endif

// C-callable functions exposed from Swift via @_cdecl in AudioBridge.swift
void audio_play_bundled_m4a(const char *name);
void audio_play_url(const char *url);
void audio_pause(void);
void audio_stop(void);

#ifdef __cplusplus
} // extern "C"
#endif
