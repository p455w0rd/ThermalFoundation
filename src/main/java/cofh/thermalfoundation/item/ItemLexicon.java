package cofh.thermalfoundation.item;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import cofh.api.item.IEmpowerableItem;
import cofh.api.item.IInventoryContainerItem;
import cofh.core.util.CoreUtils;
import cofh.core.util.KeyBindingEmpower;
import cofh.lib.util.helpers.SecurityHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.lib.util.helpers.StringHelper;
import cofh.thermalfoundation.ThermalFoundation;
import cofh.thermalfoundation.gui.GuiHandler;
import cofh.thermalfoundation.util.LexiconManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.List;

//import net.minecraft.client.renderer.texture.IIconRegister;

public class ItemLexicon extends Item implements IInventoryContainerItem, IEmpowerableItem, IBauble {

	public String modName = "thermalfoundation";
	public final String itemName;

	public ItemLexicon(String name) {

		super();
		this.itemName = name;
		setMaxDamage(1);
		setMaxStackSize(1);
		setCreativeTab(ThermalFoundation.tabCommon);
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List list) {

		ItemStack lexicon = new ItemStack(item, 1, 0);
		setEmpoweredState(lexicon, false);
		list.add(lexicon);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean check) {

		if (StringHelper.displayShiftForDetail && !StringHelper.isShiftKeyDown()) {
			list.add(StringHelper.shiftForDetails());
		}
		if (!StringHelper.isShiftKeyDown()) {
			return;
		}
		list.add(StringHelper.getInfoText("info.thermalfoundation.tome.lexicon.1"));

		if (isEmpowered(stack)) {
			list.add(StringHelper.localize("info.thermalfoundation.tome.lexicon.4") + StringHelper.END);
			list.add(StringHelper.YELLOW + StringHelper.ITALIC + StringHelper.localize("info.cofh.press") + " "
					+ StringHelper.getKeyName(KeyBindingEmpower.instance.getKey()) + " " + StringHelper.localize("info.thermalfoundation.tome.lexicon.5")
					+ StringHelper.END);
		} else {
			list.add(StringHelper.localize("info.thermalfoundation.tome.lexicon.2") + StringHelper.END);
			list.add(StringHelper.BRIGHT_BLUE + StringHelper.ITALIC + StringHelper.localize("info.cofh.press") + " "
					+ StringHelper.getKeyName(KeyBindingEmpower.instance.getKey()) + " " + StringHelper.localize("info.thermalfoundation.tome.lexicon.3")
					+ StringHelper.END);
		}
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {

		if (isEmpowered(stack)) {
			return EnumRarity.RARE;
		}
		return EnumRarity.UNCOMMON;
	}

	@Override
	public boolean isFull3D() {

		return true;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {

		if (CoreUtils.isFakePlayer(player)) {
			return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
		}
		if (ServerHelper.isServerWorld(world) && LexiconManager.getSortedOreNames().size() > 0) {
			if (isEmpowered(stack)) {
				player.openGui(ThermalFoundation.instance, GuiHandler.LEXICON_TRANSMUTE_ID, world, 0, 0, 0);
			} else {
				player.openGui(ThermalFoundation.instance, GuiHandler.LEXICON_STUDY_ID, world, 0, 0, 0);
			}
		}
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isCurrentItem) {

		if (!isEmpowered(stack)) {
			return;
		}
		NBTTagCompound tag = entity.getEntityData();
		tag.setLong("cofh.LexiconUpdate", entity.worldObj.getTotalWorldTime());
	}

	@Override
	public boolean hasCustomEntity(ItemStack stack) {

		return SecurityHelper.isSecure(stack);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {

		if (isEmpowered(stack)) {
			return "item.thermalfoundation.tome.lexicon.empowered";
		}
		return "item.thermalfoundation.tome.lexicon";
	}

	@Override
	public Entity createEntity(World world, Entity location, ItemStack stack) {

		if (SecurityHelper.isSecure(stack)) {
			location.invulnerable = true;
			location.isImmuneToFire = true;
			((EntityItem) location).lifespan = Integer.MAX_VALUE;
		}
		return null;
	}

	@Override
	public Item setUnlocalizedName(String name) {//todo

		GameRegistry.registerItem(this, name);
		name = modName + "." + name;
		return super.setUnlocalizedName(name);
	}

	public Item setUnlocalizedName(String textureName, String registrationName) {

		GameRegistry.registerItem(this, registrationName);
		textureName = modName + "." + textureName;
		return super.setUnlocalizedName(textureName);
	}

//	@Override
//	@SideOnly(Side.CLIENT)
//	public void registerIcons(IIconRegister ir) {
//
//		this.itemIcon = ir.registerIcon(modName + ":" + getUnlocalizedName().replace("item." + modName + ".", "") + "/" + StringHelper.titleCase(itemName));
//	}

	/* IInventoryContainerItem */
	@Override
	public int getSizeInventory(ItemStack container) {

		return 3;
	}

	/* IEmpowerableItem */
	@Override
	public boolean isEmpowered(ItemStack stack) {
		return !stack.hasTagCompound() ? false : stack.getTagCompound().getBoolean("Empowered");
	}

	@Override
	public boolean setEmpoweredState(ItemStack stack, boolean state) {

		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setBoolean("Empowered", state);
		return true;
	}

	@Override
	public void onStateChange(EntityPlayer player, ItemStack stack) {
		if (isEmpowered(stack)) {
			player.worldObj.playSound(player.posX, player.posY, player.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.PLAYERS, 0.4F, 1.0F, false);
		} else {
			player.worldObj.playSound(player.posX, player.posY, player.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.2F, 0.6F, false);
		}
	}

	/* IBauble */
	@Override
	public BaubleType getBaubleType(ItemStack itemstack) {

		return BaubleType.BELT;
	}

	@Override
	public void onWornTick(ItemStack itemstack, EntityLivingBase player) {

		if (!isEmpowered(itemstack)) {
			return;
		}
		NBTTagCompound tag = player.getEntityData();
		tag.setLong("cofh.LexiconUpdate", player.worldObj.getTotalWorldTime());
	}

	@Override
	public void onEquipped(ItemStack itemstack, EntityLivingBase player) {

	}

	@Override
	public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {

	}

	@Override
	public boolean canEquip(ItemStack itemstack, EntityLivingBase player) {

		return true;
	}

	@Override
	public boolean canUnequip(ItemStack itemstack, EntityLivingBase player) {

		return true;
	}

}
