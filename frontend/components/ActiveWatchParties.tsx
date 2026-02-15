import { getWatchParties } from "@/lib/watchparty";
import WatchPartyCard from "./WatchPartyCard";
import CreatePartyButton from "./CreatePartyButton";
import { getProfile } from "@/lib/auth";

export default async function ActiveWatchParties() {
  let parties: any[] = [];
  try {
    parties = await getWatchParties();
  } catch (e) {
    console.error(e);
  }

  const profile = await getProfile();

  if (parties.length === 0 && !profile) return null;

  return (
    <div className="mb-8">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-2xl font-bold">Watch Party Sobe</h2>
        {profile && <CreatePartyButton />}
      </div>
      
      {parties.length > 0 ? (
        <div className="flex flex-wrap gap-6">
          {parties.map((p: any) => (
            <WatchPartyCard key={p.id} party={p} />
          ))}
        </div>
      ) : (
        <p className="text-stone-500 italic">Trenutno nema aktivnih soba.</p>
      )}
    </div>
  );
}