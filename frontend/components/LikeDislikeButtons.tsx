"use client";

import { useState } from "react";
import { usePathname } from "next/navigation";
import { Button } from "@/components/ui/button";
import { ThumbsDown, ThumbsUp } from "@carbon/icons-react";
import { dislikeVideo, likeVideo } from "@/lib/videofeed";

interface LikeDislikeButtonsProps {
  videoId: number;
  initialLikes: number;
  initialDislikes: number;
  initialLiked: boolean;
  initialDisliked: boolean;
}

export default function LikeDislikeButtons({
  videoId,
  initialLikes,
  initialDislikes,
  initialLiked,
  initialDisliked,
}: LikeDislikeButtonsProps) {
  const [likes, setLikes] = useState(initialLikes);
  const [dislikes, setDislikes] = useState(initialDislikes);
  const [liked, setLiked] = useState(initialLiked);
  const [disliked, setDisliked] = useState(initialDisliked);

  const pathname = usePathname();

  const handleLike = async () => {
    const wasLiked = liked;
    const wasDisliked = disliked;
    const originalLikes = likes;
    const originalDislikes = dislikes;

    // Optimistic update
    if (wasLiked) {
      setLiked(false);
      setLikes(likes - 1);
    } else {
      setLiked(true);
      setLikes(likes + 1);
      if (wasDisliked) {
        setDisliked(false);
        setDislikes(dislikes - 1);
      }
    }

    try {
      await likeVideo(videoId, pathname);
    } catch (error) {
      // Revert on error
      setLiked(wasLiked);
      setLikes(originalLikes);
      setDisliked(wasDisliked);
      setDislikes(originalDislikes);
      console.error("Failed to like video", error);
    }
  };

  const handleDislike = async () => {
    const wasLiked = liked;
    const wasDisliked = disliked;
    const originalLikes = likes;
    const originalDislikes = dislikes;

    // Optimistic update
    if (wasDisliked) {
      setDisliked(false);
      setDislikes(dislikes - 1);
    } else {
      setDisliked(true);
      setDislikes(dislikes + 1);
      if (wasLiked) {
        setLiked(false);
        setLikes(likes - 1);
      }
    }

    try {
      await dislikeVideo(videoId, pathname);
    } catch (error) {
      // Revert on error
      setDisliked(wasDisliked);
      setDislikes(originalDislikes);
      setLiked(wasLiked);
      setLikes(originalLikes);
      console.error("Failed to dislike video", error);
    }
  };

  return (
    <div className="flex gap-2 text-sm pt-0">
      <Button
        size="sm"
        className={`transition-colors duration-200 ${
          liked ? "bg-lime-400 hover:bg-lime-500" : "bg-lime-500/40"
        }`}
        onClick={handleLike}
      >
        <ThumbsUp /> {likes}
      </Button>
      <Button
        size="sm"
        className={`transition-colors duration-200 ${
          disliked
            ? "bg-red-600/70 text-white hover:bg-red-600/90"
            : "bg-red-500/50"
        }`}
        onClick={handleDislike}
      >
        <ThumbsDown /> {dislikes}
      </Button>
    </div>
  );
}
