"use client";

import { useEffect, useRef } from "react";
import { useRouter } from "next/navigation";
import { viewVideo } from "@/lib/videofeed";

export default function ViewTracker({ videoId }: { videoId: number }) {
  const hasViewed = useRef(false);
  const router = useRouter();

  useEffect(() => {
    if (hasViewed.current) return;

    viewVideo(videoId)
      .then(() => {
        router.refresh();
      })
      .catch((err) =>
        console.error("Failed to count view", err)
      );

    hasViewed.current = true;
  }, [videoId, router]);

  return null;
}