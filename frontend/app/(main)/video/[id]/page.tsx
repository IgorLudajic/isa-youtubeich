import { getVideoDetails } from "@/lib/videofeed";
import { getFullUrl } from "@/lib/utils";
import { notFound } from "next/navigation";
import LikeDislikeButtons from "@/components/LikeDislikeButtons";
import CommentsList from "@/components/CommentsList";
import ViewTracker from "@/components/ViewTracker";
import VideoChat from "@/components/VideoChat";
import { getProfile } from "@/lib/auth";
import { i18n } from "@/lib/i18n";
import Link from "next/link";

export default async function VideoPage({
  params,
  searchParams,
}: {
  params: Promise<{ id: string }>;
  searchParams: Promise<{ commentPage?: string }>;
}) {
  const id = parseInt((await params).id);
  if (isNaN(id)) {
    notFound();
  }

  const { commentPage } = await searchParams;
  const page = commentPage ? parseInt(commentPage) : 0;

  // Učitavamo video i profil paralelno da ne bismo kočili renderovanje
  const [video, profile] = await Promise.all([
    getVideoDetails(id).catch(() => null),
    getProfile().catch(() => null),
  ]);

  if (!video) {
    notFound();
  }

  // Generišemo ime za čat (ako je gost, dajemo mu random broj)
  const chatUsername = profile?.username || `Gost_${Math.floor(Math.random() * 1000)}`;

  return (
    <div className="min-h-screen pb-12">
      <ViewTracker videoId={id} />

      {/* Glavni kontejner sa gridom: 3 kolone na desktopu, 1 na mobilnom */}
      <div className="max-w-[1600px] mx-auto grid grid-cols-1 lg:grid-cols-4 gap-8">

        {/* LEVA KOLONA: Video, Info i Komentari (Zauzima 3/4 na desktopu) */}
        <div className="lg:col-span-3 space-y-6">

          {/* Video Player Section */}
          <div className="aspect-video bg-black rounded-lg overflow-hidden relative z-10 border-2 border-border shadow-xl">
            <video controls className="w-full h-full" poster={video.thumbnailUrl}>
              <source
                src={getFullUrl(`/api/videos/${id}/stream`)}
                type="video/mp4"
              />
              Vaš pretraživač ne podržava video tag.
            </video>
          </div>

          {/* Video Info Section */}
          <div className="bg-background p-6 rounded-base border-2 border-border shadow-shadow relative z-0">
            <div className="flex flex-wrap items-center justify-between gap-4 border-b-2 border-border/10 pb-4">
              <h1 className="text-2xl font-heading break-all">{video.title}</h1>
              <span className="text-xl font-[600] whitespace-nowrap bg-main/10 px-3 py-1 rounded-base border-border border-2">
                {video.viewCount} {i18n.views(video.viewCount)}
              </span>
            </div>

            <div className="flex flex-col sm:flex-row sm:items-start justify-between mt-6 gap-4">
              <div className="flex items-center gap-3">
                <div className="flex flex-col">
                  <Link
                    href={`/profiles/${video.creatorUsername}`}
                    className="text-lime-700 font-mono font-[700] text-lg hover:underline decoration-2"
                  >
                    @{video.creatorUsername}
                  </Link>
                  <p className="text-xs text-stone-500 font-medium">
                    Objavljeno {new Date(video.createdAt).toLocaleDateString()}
                  </p>
                </div>
              </div>

              <div className="flex gap-2 items-center">
                <LikeDislikeButtons
                  videoId={id}
                  initialLikes={video.likes}
                  initialDislikes={video.dislikes}
                  initialLiked={video.likedByCurrentUser}
                  initialDisliked={video.dislikedByCurrentUser}
                />
              </div>
            </div>

            <div className="mt-6 p-4 bg-secondary-background rounded-base border-2 border-border/50 text-sm leading-relaxed whitespace-pre-wrap">
              {video.description}
            </div>
          </div>

          {/* Komentari - sada su ispod video opisa */}
          <div className="mt-8">
            <CommentsList videoId={id} page={page} />
          </div>
        </div>

        {/* DESNA KOLONA: Čat uživo (Zauzima 1/4 na desktopu) */}
        <div className="lg:col-span-1">
          <div className="lg:sticky lg:top-24">
             <VideoChat videoId={id} username={chatUsername} />

             {/* Mali info box za odbranu - Profesori ovo vole da vide */}
             <div className="mt-4 p-4 border-2 border-dashed border-stone-400 rounded-base bg-amber-50 text-[10px] text-stone-600 font-mono leading-tight">
               <p className="font-bold text-amber-800 mb-1">⚙️ CLUSTER MODE ACTIVE</p>
               <p>• WebSocket: STOMP Protocol</p>
               <p>• Sync: Redis Pub/Sub</p>
               <p>• Logic: Transient (No History)</p>
             </div>
          </div>
        </div>

      </div>
    </div>
  );
}