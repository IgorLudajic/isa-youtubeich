import { Suspense } from "react";
import { getVideoComments } from "@/lib/videofeed";
import CommentForm from "./CommentForm";
import CommentsPagination from "./CommentsPagination";
import CommentsSkeleton from "./CommentsSkeleton";
import Link from "next/link";

interface CommentsListOnlyProps {
  videoId: number;
  page: number;
}

async function CommentsListOnly({ videoId, page }: CommentsListOnlyProps) {
  const commentsPage = await getVideoComments(videoId, page, 10);

  return (
    <>
      <div className="space-y-4">
        {commentsPage.content.map((comment) => (
          <div
            key={comment.id}
            className="flex gap-4 bg-secondary-background border-border shadow-shadow rounded-base border-2 p-4"
          >
            <div className="h-10 w-10 bg-gray-300 rounded-full flex items-center justify-center border-2">
              {comment.username[0].toUpperCase()}
            </div>
            <div className="flex-1 -mt-1">
              <div className="flex items-baseline gap-2">
                <Link
                  href={`/profiles/${comment.username}`}
                  className="font-semibold hover:underline decoration-2 underline-offset-4"
                >
                  {comment.username}
                </Link>
                <span className="text-sm text-gray-500">
                  {new Date(comment.createdAt).toLocaleDateString()}
                </span>
              </div>
              <p className="text-sm">{comment.text}</p>
            </div>
          </div>
        ))}
      </div>
      <CommentsPagination
        totalPages={commentsPage.totalPages}
        currentPage={page}
        videoId={videoId}
      />
    </>
  );
}

interface CommentsListProps {
  videoId: number;
  page: number;
}

export default function CommentsList({ videoId, page }: CommentsListProps) {
  return (
    <div className="space-y-4">
      <h2 className="text-xl font-heading">Komentari</h2>
      <CommentForm videoId={videoId} />
      <div className="mt-8" />
      <Suspense fallback={<CommentsSkeleton />}>
        <CommentsListOnly videoId={videoId} page={page} />
      </Suspense>
    </div>
  );
}
