"use client";

import { useEffect, useRef } from "react";
import { viewVideo } from "@/lib/videofeed";

export default function ViewTracker({ videoId }: { videoId: number }) {
  const hasViewed = useRef(false);

  useEffect(() => {
    if (hasViewed.current) return;

    viewVideo(videoId).catch((err) =>
      console.error("Failed to count view", err),
    );

    hasViewed.current = true;
  }, [videoId]);

  return null;
}
