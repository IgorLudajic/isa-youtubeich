"use client";

import { useState } from "react";
import { usePathname, useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { postComment } from "@/lib/videofeed";

interface CommentFormProps {
  videoId: number;
}

export default function CommentForm({ videoId }: CommentFormProps) {
  const [text, setText] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null); // Added error state
  const router = useRouter();
  const pathname = usePathname();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!text.trim()) return;

    setIsSubmitting(true);
    setError(null); // Reset error on new attempt

    try {
      await postComment(videoId, text.trim(), pathname);
      setText("");
      router.refresh();
    } catch (err: unknown) {
      console.error("Failed to post comment", err);
      setError("You have exceeded the limit of 60 comments per hour.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-2">
      <Textarea
        placeholder="Dodaj komentar..."
        value={text}
        onChange={(e) => {
          setText(e.target.value);
          if (error) setError(null);
        }}
        className="min-h-20"
      />

      {error && (
        <p className="text-sm font-medium text-destructive bg-destructive/10 p-2 rounded-base border-2 border-destructive">
          {error}
        </p>
      )}

      <Button type="submit" disabled={isSubmitting || !text.trim()}>
        {isSubmitting ? "Objavljivanje..." : "Objavi"}
      </Button>
    </form>
  );
}
