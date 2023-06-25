package hello.springcommunity.web.item;

import hello.springcommunity.domain.item.*;
import hello.springcommunity.web.item.form.ItemSaveForm;
import hello.springcommunity.web.item.form.ItemUpdateForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemRepository itemRepository;
    private final ItemService itemService;

    /**
     * regions(등록 지역), itemTypes(상품 종류), DeliveryCode(배송 방식) 은 등록 폼, 상세화면, 수정 폼 에서 모두 사용하므로
     * @ModelAttribute 를 사용하여 컨트롤러의 별도의 메서드에 적용하면, 컨트롤러가 호출될 때마다 반환값(regions,itemTypes,deliveryCodes)이 자동으로 model에 담기게 된다
     * @return
     */
    @ModelAttribute("regions")
    public Map<String, String> regions() {
        Map<String, String> regions = new LinkedHashMap<>();
        regions.put("SEOUL", "서울");
        regions.put("BUSAN", "부산");
        regions.put("JEJU", "제주");
        return regions;
    }

    @ModelAttribute("itemTypes")
    public ItemType[] itemTypes() {
        return ItemType.values(); //해당 ENUM 의 모든 정보를 배열로 반환 [BOOK,FOOD,ETC]
    }

    @ModelAttribute("deliveryCodes")
    public List<DeliveryCode> deliveryCodes() {
        List<DeliveryCode> deliveryCodes = new ArrayList<>();
        deliveryCodes.add(new DeliveryCode("FAST", "빠른 배송"));
        deliveryCodes.add(new DeliveryCode("NORMAL", "일반 배송"));
        deliveryCodes.add(new DeliveryCode("SLOW", "느린 배송"));
        return deliveryCodes;
    }

//    @GetMapping
//    public String items(Model model) {
//        List<Item> items = itemRepository.findAll();
//        model.addAttribute("items", items);
//        return "items/items";
//    }

    @GetMapping
    public String items(@ModelAttribute("itemSearch") ItemSearchCond itemSearch, Model model) {
        List<Item> items = itemService.findItems(itemSearch);
        model.addAttribute("items", items);
        return "items/items";
    }


//    @GetMapping("/{itemId}")
//    public String item(@PathVariable long itemId, Model model) {
//        Item item = itemRepository.findById(itemId);
//        model.addAttribute("item", item);
//        return "items/item";
//    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemService.findById(itemId).get();
        model.addAttribute("item", item);
        return "items/itemOnly";
    }


//    @GetMapping("/add")
//    public String addForm(Model model) {
//        model.addAttribute("item", new Item());
//        return "items/addForm";
//    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "items/addFormOnly";
    }


//    @PostMapping("/add")
//    public String addItem(@Validated @ModelAttribute("item") ItemSaveForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
//
//        //특정 필드 예외가 아닌 전체 예외
//        if (form.getPrice() != null && form.getQuantity() != null) {
//            int resultPrice = form.getPrice() * form.getQuantity();
//            if (resultPrice < 10000) {
//                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
//            }
//        }
//
//        if (bindingResult.hasErrors()) {
//            log.info("errors={}", bindingResult);
//            return "items/addForm";
//        }
//
//        //성공 로직
//        log.info("item.open={}", form.getOpen());
//        log.info("item.regions={}", form.getRegions());
//        log.info("item.itemType={}", form.getItemType());
//        log.info("item.deliveryCode={}", form.getDeliveryCode());
//
//
//        Item item = new Item();
//        item.setItemName(form.getItemName());
//        item.setPrice(form.getPrice());
//        item.setQuantity(form.getQuantity());
//        item.setOpen(form.getOpen());
//        item.setRegions(form.getRegions());
//        item.setItemType(form.getItemType());
//        item.setDeliveryCode(form.getDeliveryCode());
//
//        Item savedItem = itemRepository.save(item);
//        redirectAttributes.addAttribute("itemId", savedItem.getId());
//        redirectAttributes.addAttribute("status", true);
//        return "redirect:/items/{itemId}";
//    }

    @PostMapping("/add")
    public String addItem(@ModelAttribute("item") Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            return "items/addFormOnly";
        }

        log.info("item={}", item);

        Item savedItem = itemService.save(item);
        log.info("savedItem={}", savedItem);

        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/items/{itemId}";
    }


//    @GetMapping("/{itemId}/edit")
//    public String editForm(@PathVariable Long itemId, Model model) {
//        Item item = itemRepository.findById(itemId);
//        model.addAttribute("item", item);
//        return "items/editForm";
//    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemService.findById(itemId).get();
        model.addAttribute("item", item);
        return "items/editFormOnly";
    }


//    @PostMapping("/{itemId}/edit")
//    public String edit(@PathVariable Long itemId, @Validated @ModelAttribute("item") ItemUpdateForm form, BindingResult bindingResult) {
//
//        //특정 필드 예외가 아닌 전체 예외
//        if (form.getPrice() != null && form.getQuantity() != null) {
//            int resultPrice = form.getPrice() * form.getQuantity();
//            if (resultPrice < 10000) {
//                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
//            }
//        }
//
//        if (bindingResult.hasErrors()) {
//            log.info("errors={}", bindingResult);
//            return "items/editForm";
//        }
//
//        Item itemParam = new Item();
//        itemParam.setItemName(form.getItemName());
//        itemParam.setPrice(form.getPrice());
//        itemParam.setQuantity(form.getQuantity());
//        itemParam.setOpen(form.getOpen());
//        itemParam.setRegions(form.getRegions());
//        itemParam.setItemType(form.getItemType());
//        itemParam.setDeliveryCode(form.getDeliveryCode());
//
//        itemRepository.update(itemId, itemParam);
//        return "redirect:/items/{itemId}";
//    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute("item") ItemUpdateDto updateParam, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            return "items/editFormOnly";
        }

        log.info("updateParam={}", updateParam);

        itemService.update(itemId, updateParam);
        return "redirect:/items/{itemId}";
    }
}
