"use client";

import Hls from "hls.js";
import React, { useEffect, useMemo, useRef, useState } from "react";

type Props = {
  src: string;
  poster?: string;
  className?: string;
  autoPlay?: boolean;
  muted?: boolean;
};

type HlsErrorData = {
  fatal: boolean;
  type?: string;
};

export default function HlsVideoPlayer({
  src,
  poster,
  className,
  autoPlay = false,
  muted = false,
}: Props) {
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const [staticError, setStaticError] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Keep it deterministic so it doesn't re-init unnecessarily.
  const canUseNativeHls = useMemo(() => {
    if (typeof document === "undefined") return false;
    const v = document.createElement("video");
    return (
      v.canPlayType("application/vnd.apple.mpegURL") ||
      v.canPlayType("application/x-mpegURL")
    );
  }, []);

  useEffect(() => {
    const video = videoRef.current;
    if (!video) return;

    // Safari/iOS: native HLS.
    if (canUseNativeHls) {
      queueMicrotask(() => setStaticError(null));
      video.src = src;
      if (autoPlay) {
        video.play().catch(() => {
          /* ignored */
        });
      }
      return;
    }

    // Other browsers: Hls.js via MSE.
    if (!Hls.isSupported()) {
      queueMicrotask(() =>
        setStaticError(
          "This browser can't play HLS streams. Try Safari or open the stream in an HLS-capable player.",
        ),
      );
      return;
    }

    queueMicrotask(() => setStaticError(null));

    // Clear any previous error now that we're trying to load again.
    queueMicrotask(() => setError(null));

    const hls = new Hls({
      enableWorker: true,
      lowLatencyMode: true,
      // Stay close to the live edge.
      liveSyncDuration: 1.5,
      liveMaxLatencyDuration: 3,
      // Don't keep huge buffers behind live.
      backBufferLength: 10,
      maxBufferLength: 5,
      maxMaxBufferLength: 10,
      liveDurationInfinity: true,
    });

    hls.attachMedia(video);

    const onMediaAttached = () => {
      hls.loadSource(src);
    };

    const onError = (_evt: unknown, data: any) => {
      const d: HlsErrorData = data;
      if (!d?.fatal) return;

      switch (d.type) {
        case "networkError":
        case "NETWORK_ERROR":
          setError("Network error while loading the stream.");
          try {
            hls.startLoad();
          } catch {
            /* ignored */
          }
          break;
        case "mediaError":
        case "MEDIA_ERROR":
          setError("Media error while decoding the stream.");
          try {
            hls.recoverMediaError();
          } catch {
            /* ignored */
          }
          break;
        default:
          setError("Unrecoverable error while playing the stream.");
          hls.destroy();
      }
    };

    hls.on(Hls.Events.MEDIA_ATTACHED, onMediaAttached);
    hls.on(Hls.Events.ERROR, onError);

    return () => {
      hls.off(Hls.Events.MEDIA_ATTACHED, onMediaAttached);
      hls.off(Hls.Events.ERROR, onError);
      hls.destroy();
    };
  }, [src, autoPlay, canUseNativeHls]);

  const displayError = error ?? staticError;

  return (
    <div className="w-full h-full">
      <video
        ref={videoRef}
        controls
        playsInline
        muted={muted}
        autoPlay={autoPlay}
        className={className}
        poster={poster}
      />

      {displayError ? (
        <div className="mt-2 text-sm text-red-600">{displayError}</div>
      ) : null}
    </div>
  );
}
