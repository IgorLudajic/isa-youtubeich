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
  const router = useRouter();
  const pathname = usePathname();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!text.trim()) return;

    setIsSubmitting(true);
    try {
      await postComment(videoId, text.trim(), pathname);
      setText("");
      router.refresh(); // Re-render the page to show new comment
    } catch (error) {
      console.error("Failed to post comment", error);
      // TODO: show error message
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-2">
      <Textarea
        placeholder="Dodaj komentar..."
        value={text}
        onChange={(e) => setText(e.target.value)}
        className="min-h-[80px]"
      />
      <Button type="submit" disabled={isSubmitting || !text.trim()}>
        Objavi
      </Button>
    </form>
  );
}
