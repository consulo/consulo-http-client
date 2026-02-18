package org.javamaster.httpclient.impl.symbol;

import consulo.application.internal.ProgressIndicatorUtils;
import consulo.application.progress.ProgressIndicator;
import consulo.application.util.matcher.NameUtil;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.DumbService;
import consulo.project.Project;
import consulo.searchEverywhere.FoundItemDescriptor;
import consulo.searchEverywhere.SearchEverywhereContributor;
import consulo.searchEverywhere.SearchEverywhereContributorFactory;
import consulo.searchEverywhere.SearchEverywhereManager;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import org.javamaster.httpclient.model.HttpMethod;
import org.javamaster.httpclient.impl.psi.impl.RequestNavigationItem;
import org.javamaster.httpclient.impl.scan.ScanRequest;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author yudong
 */
public class ApisGotoSEContributor extends AbstractGotoSEContributor implements PossibleSlowContributor {
    private final PersistentSearchEverywhereContributorFilter<HttpMethod> methodFilter;
    private final Set<HttpMethod> filterMethods = new HashSet<>();
    private final DumbService dumbService;

    public ApisGotoSEContributor(AnActionEvent event) {
        super(event);
        filterMethods.addAll(HttpMethod.getMethods());
        dumbService = myProject.getService(DumbService.class);

        methodFilter = new PersistentSearchEverywhereContributorFilter<>(
                HttpMethod.getMethods(),
                new ChooseByNameFilterConfiguration<HttpMethod>() {
                    @Override
                    protected String nameForElement(HttpMethod type) {
                        return type.name();
                    }

                    @Override
                    public boolean isVisible(HttpMethod type) {
                        return true;
                    }

                    @Override
                    public void setVisible(HttpMethod type, boolean value) {
                        if (value) {
                            filterMethods.add(type);
                        } else {
                            filterMethods.remove(type);
                        }
                    }
                },
                type -> type.name(),
                type -> type.getIcon()
        );
    }

    @Override
    public void fetchWeightedElements(
            @NotNull String pattern,
            @NotNull ProgressIndicator progressIndicator,
            @NotNull Processor<? super FoundItemDescriptor<Object>> consumer
    ) {
        Runnable fetchRunnable = () -> {
            if (dumbService.isDumb()) {
                return;
            }

            if (shouldNotProvideElements()) {
                return;
            }

            NameUtil.MinusculeMatcher matcher = NameUtil.buildMatcher("*" + pattern, NameUtil.MatchingCaseSensitivity.NONE);

            GlobalSearchScope scope = getScope().getScope() instanceof GlobalSearchScope ?
                    (GlobalSearchScope) getScope().getScope() :
                    GlobalSearchScope.projectScope(myProject);

            ScanRequest.fetchRequests(myProject, scope, requestInfo -> {
                progressIndicator.checkCanceled();

                if (requestInfo.getPsiElement() != null &&
                        filterMethods.contains(requestInfo.getMethod()) &&
                        matcher.matches(requestInfo.getPath())) {
                    consumer.process(new FoundItemDescriptor<>(new RequestNavigationItem(requestInfo), 100));
                }
            });
        };

        @SuppressWarnings({"deprecation", "UsagesOfObsoleteApi"})
        boolean unused1 = ProgressIndicatorUtils.yieldToPendingWriteActions();
        @SuppressWarnings({"deprecation", "UsagesOfObsoleteApi"})
        boolean unused2 = ProgressIndicatorUtils.runInReadActionWithWriteActionPriority(fetchRunnable, progressIndicator);
    }

    @NotNull
    @Override
    public FilteringGotoByModel<?> createModel(@NotNull Project project) {
        return new GotoApiModel2(project, this);
    }

    @NotNull
    @Override
    public List<AnAction> getActions(@NotNull Runnable onChanged) {
        return doGetActions(methodFilter, null, onChanged);
    }

    private boolean shouldNotProvideElements() {
        SearchEverywhereManager seManager = SearchEverywhereManager.getInstance(myProject);
        if (!seManager.isShown()) {
            return true;
        }

        return !getSearchProviderId().equals(seManager.getSelectedTabID());
    }

    @NotNull
    @Override
    public String getGroupName() {
        return "Apis";
    }

    @Override
    public int getSortWeight() {
        return 800;
    }

    @Override
    public boolean isEmptyPatternSupported() {
        return true;
    }

    public static class Factory implements SearchEverywhereContributorFactory<Object> {
        @NotNull
        @Override
        public SearchEverywhereContributor<Object> createContributor(@NotNull AnActionEvent initEvent) {
            ApisGotoSEContributor seContributor = new ApisGotoSEContributor(initEvent);

            return new PSIPresentationBgRendererWrapper<>(seContributor);
        }
    }
}
