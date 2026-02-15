"use client";

import { useEffect, useState } from "react";
import { getHomeFeed, VideoHomeDto } from "@/lib/videofeed";
import VideoGrid from "./VideoGrid";
import SimplePagination from "./SimplePagination";
import { Button } from "@/components/ui/button";
import { PlayFilledAlt } from "@carbon/icons-react";

interface PartyVideoPickerProps {
  onPlayVideo: (videoId: number) => void;
}

export default function PartyVideoPicker({ onPlayVideo }: PartyVideoPickerProps) {
  const [videos, setVideos] = useState<VideoHomeDto[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [selectedVideo, setSelectedVideo] = useState<VideoHomeDto | null>(null);

  useEffect(() => {
    loadVideos(page);
  }, [page]);

  const loadVideos = async (p: number) => {
    setLoading(true);
    try {
      // Fetch simpler page size (4 or 6) for the "dashboard" feel
      const data = await getHomeFeed(p, 4);
      setVideos(data.content);
      setTotalPages(data.totalPages);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-purple-100 p-6 rounded-base border-2 border-purple-300 mt-6 text-left">
      <div className="flex justify-between items-center mb-4">
          <h3 className="font-bold flex items-center gap-2 text-xl">
             <PlayFilledAlt /> Izaberite video
          </h3>
          {selectedVideo && (
            <div className="flex items-center gap-2 bg-white px-3 py-1 rounded-base border-2 border-black animate-in fade-in slide-in-from-right-5">
                <span className="text-sm font-bold truncate max-w-[150px]">{selectedVideo.title}</span>
                <Button 
                    size="sm" 
                    className="bg-red-500 hover:bg-red-600 text-white h-7"
                    onClick={() => onPlayVideo(selectedVideo.Id)}
                >
                    PUSTI ODMAH
                </Button>
            </div>
          )}
      </div>

      {loading ? (
        <div className="h-40 flex items-center justify-center italic text-gray-500">Učitavanje snimaka...</div>
      ) : (
        <VideoGrid videos={videos} onSelect={(v) => setSelectedVideo(v)}>
             <SimplePagination 
                currentPage={page} 
                totalPages={totalPages} 
                onPageChange={setPage} 
             />
        </VideoGrid>
      )}
      
      <p className="text-xs text-center mt-4 text-gray-500">
        *Kliknite na karticu videa da ga izaberete, a zatim kliknite "Pusti Odmah" u gornjem desnom uglu.
      </p>
    </div>
  );
}