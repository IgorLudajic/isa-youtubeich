import Image from "next/image";
import { cn } from "@/lib/utils";

type Props = {
  imageUrl: string;
  children: React.ReactNode;
  className?: string;
};

export default function ImageCard({ imageUrl, children, className }: Props) {
  return (
    <figure
      className={cn(
        "md:w-[250px] overflow-hidden rounded-base border-2 border-border bg-main font-base shadow-shadow",
        className,
      )}
    >
      <Image
        unoptimized
        src={imageUrl}
        alt="image"
        width={250}
        height={187.5}
        className="w-full aspect-4/3 object-cover"
      />
      <figcaption className="border-t-2 text-main-foreground border-border p-4">
        {children}
      </figcaption>
    </figure>
  );
}
