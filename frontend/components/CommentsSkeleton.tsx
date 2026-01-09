import { Skeleton } from "@/components/ui/skeleton";

export default function CommentsSkeleton() {
  return (
    <div className="space-y-4 opacity-70">
      {Array.from(Array(4).keys()).map((_, i) => (
        <Skeleton
          key={i}
          className="flex gap-4 p-4 rounded-base border-2 border-border bg-secondary-background shadow-shadow"
        >
          <Skeleton className="h-10 w-10 rounded-full bg-gray-200" />
          <div className="space-y-2 flex-1">
            <Skeleton className="h-4 w-1/4 bg-gray-200" />
            <Skeleton className="h-4 w-full bg-gray-200" />
          </div>
        </Skeleton>
      ))}
    </div>
  );
}
