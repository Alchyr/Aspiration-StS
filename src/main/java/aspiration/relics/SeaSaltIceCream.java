package aspiration.relics;

import aspiration.Aspiration;
import aspiration.relics.abstracts.AspirationRelic;
import aspiration.ui.screens.RelicSelectScreen;
import aspiration.vfx.ObtainRelicLater;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.ConfigUtils;
import com.google.gson.JsonSyntaxException;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class SeaSaltIceCream extends AspirationRelic {
    public static final String ID = "aspiration:SeaSaltIceCream";
    private boolean relicSelected = true;
    private RelicSelectScreen relicSelectScreen;
    private boolean fakeHover = false;
    private static boolean debug = false;

    public SeaSaltIceCream() {
        super(ID, "SeaSaltIceCream.png", RelicTier.UNCOMMON, LandingSound.FLAT);
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0] + FontHelper.colorString(RelicLibrary.getRelic(getSavedItem()).name, "y");
    }

    @Override
    public void onEquip()
    {
        if (AbstractDungeon.isScreenUp) {
            AbstractDungeon.dynamicBanner.hide();
            AbstractDungeon.overlayMenu.cancelButton.hide();
            AbstractDungeon.previousScreen = AbstractDungeon.screen;
        }
        AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.INCOMPLETE;

        openRelicSelect();
    }

    private void openRelicSelect()
    {
        relicSelected = false;

        ArrayList<AbstractRelic> relics = new ArrayList<>();
        for (AbstractRelic r : AbstractDungeon.player.relics) {
            if (!r.relicId.equals(ID)) {
                AbstractRelic re = r.makeCopy();
                re.isSeen = true;
                relics.add(re);
            }
        }

        relicSelectScreen = new RelicSelectScreen();
        relicSelectScreen.open(relics);


    }

    @Override
    public void update()
    {
        super.update();

        if (!relicSelected) {
            if (relicSelectScreen.doneSelecting()) {
                relicSelected = true;

                AbstractRelic tmp = RelicLibrary.getRelic(getSavedItem()).makeCopy();
                if(debug) System.out.println("tmp is: " +tmp.relicId);
                switch (tmp.tier) {
                    case COMMON:
                        AbstractDungeon.commonRelicPool.removeIf(id -> id.equals(tmp.relicId));
                        break;
                    case UNCOMMON:
                        AbstractDungeon.uncommonRelicPool.removeIf(id -> id.equals(tmp.relicId));
                        break;
                    case RARE:
                        AbstractDungeon.rareRelicPool.removeIf(id -> id.equals(tmp.relicId));
                        break;
                    case SHOP:
                        AbstractDungeon.shopRelicPool.removeIf(id -> id.equals(tmp.relicId));
                        break;
                    case BOSS:
                        AbstractDungeon.bossRelicPool.removeIf(id -> id.equals(tmp.relicId));
                        break;
                    default:
                        break;
                    }
                AbstractDungeon.effectsQueue.add(0, new ObtainRelicLater(tmp));

                AbstractRelic relic = relicSelectScreen.getSelectedRelics().get(0).makeCopy();

                try {
                    Files.write(Paths.get(getSavePath()), relic.relicId.getBytes());
                } catch (IOException e) {
                    Aspiration.logger.error(e);
                }

                AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
            } else {
                relicSelectScreen.update();
                if (!hb.hovered) {
                    fakeHover = true;
                }
                hb.hovered = true;
            }
        }
    }

    public static String getSavedItem()
    {
        try {
            if (Gdx.files.absolute(getSavePath()).exists()) {
                //Gson gson = new Gson();
                String savestr = loadSaveString(getSavePath());
                if(debug) System.out.println("Savestr is:" + savestr);
                //String save = gson.fromJson(savestr, String.class);
                try {
                    //if(debug) System.out.println("Save is: " + save);
                    return savestr;
                } catch (Exception e) {
                    Aspiration.logger.error("Failed to get saved SSIC relic String \"" + savestr + "\"");
                    return Nostalgia.ID;
                }
            }
        } catch (JsonSyntaxException e) {
            Aspiration.logger.error(e);
            SeaSaltIceCream.deleteSave();
            return Nostalgia.ID;
        }
        return Nostalgia.ID;
    }

    public static void deleteSave()
    {
        Gdx.files.absolute(getSavePath()).delete();
    }

    private static String loadSaveString(String filePath)
    {
        FileHandle file = Gdx.files.absolute(filePath);
        String data = file.readString();
        if(debug) System.out.println("Data is: "+ data);
        /*if (SaveFileObfuscator.isObfuscated(data)) {
            return SaveFileObfuscator.decode(data, "key");
        }*/
        return data;
    }

    private static String getSavePath()
    {
        return ConfigUtils.CONFIG_DIR + File.separator + "Aspiration" + File.separator + "SeaSaltIceCream" + ".autosave" + (Settings.isBeta ? "BETA" : "");
    }

    @Override
    public void renderTip(SpriteBatch sb)
    {
        if (!relicSelected && fakeHover) {
            relicSelectScreen.render(sb);
        }
        if (fakeHover) {
            fakeHover = false;
            hb.hovered = false;
        } else {
            super.renderTip(sb);
        }
    }

    @Override
    public void renderInTopPanel(SpriteBatch sb)
    {
        super.renderInTopPanel(sb);

        if (!relicSelected && !fakeHover) {
            relicSelectScreen.render(sb);
        }
    }

    public AbstractRelic makeCopy() {
        return new SeaSaltIceCream();
    }
}